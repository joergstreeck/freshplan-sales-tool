# 👁️ FC-033 VISUAL CUSTOMER CARDS - API & DATENMODELL

[← Zurück zur Übersicht](/docs/features/ACTIVE/01_security/FC-033_OVERVIEW.md)

## 🔌 API ENDPOINTS {#endpoints}

### Photo Management

```typescript
// POST /api/customers/{customerId}/photos
// Upload a new photo for a customer
interface UploadPhotoRequest {
  photo: File; // multipart/form-data
  type: 'contact' | 'building' | 'note' | 'product';
  contactId?: string; // für contact photos
  caption?: string;
}

interface UploadPhotoResponse {
  id: string;
  url: string;
  thumbnailUrl: string;
  type: string;
  uploadedAt: string;
}

// GET /api/customers/{customerId}/photos
// Get all photos for a customer
interface GetPhotosResponse {
  photos: Photo[];
  totalCount: number;
}

// DELETE /api/customers/{customerId}/photos/{photoId}
// Delete a specific photo

// PATCH /api/customers/{customerId}/photos/{photoId}
// Update photo metadata
interface UpdatePhotoRequest {
  caption?: string;
  tags?: string[];
}
```

### Memory Aids & Visual Notes

```typescript
// GET /api/customers/{customerId}/memory-aids
interface MemoryAid {
  id: string;
  customerId: string;
  icon: string; // emoji or icon identifier
  text: string;
  createdBy: string;
  createdAt: string;
}

// POST /api/customers/{customerId}/memory-aids
interface CreateMemoryAidRequest {
  icon: string;
  text: string;
}

// DELETE /api/customers/{customerId}/memory-aids/{aidId}
```

## 📊 DATENMODELL

### Database Schema

```sql
-- Photos table
CREATE TABLE customer_photos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
  contact_id UUID REFERENCES customer_contacts(id) ON DELETE SET NULL,
  type VARCHAR(50) NOT NULL CHECK (type IN ('contact', 'building', 'note', 'product')),
  url TEXT NOT NULL,
  thumbnail_url TEXT NOT NULL,
  caption TEXT,
  mime_type VARCHAR(100),
  size_bytes INTEGER,
  width INTEGER,
  height INTEGER,
  uploaded_by UUID NOT NULL REFERENCES users(id),
  uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  tags TEXT[],
  metadata JSONB DEFAULT '{}'::jsonb,
  
  CONSTRAINT unique_contact_photo UNIQUE NULLS NOT DISTINCT (contact_id, type)
);

-- Memory aids table
CREATE TABLE customer_memory_aids (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
  icon VARCHAR(50) NOT NULL,
  text VARCHAR(255) NOT NULL,
  created_by UUID NOT NULL REFERENCES users(id),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  
  INDEX idx_memory_aids_customer (customer_id)
);

-- Visual metadata for customers
ALTER TABLE customers ADD COLUMN IF NOT EXISTS visual_metadata JSONB DEFAULT '{}'::jsonb;
-- Structure: {
--   "buildingType": "modern" | "altbau" | "chain",
--   "logoColors": ["#FF0000", "#FFFFFF"],
--   "visualTags": ["roter-eingang", "eckgebäude", "dachterrasse"],
--   "preferredProducts": ["product-id-1", "product-id-2"]
-- }
```

### JPA Entities

```java
@Entity
@Table(name = "customer_photos")
public class CustomerPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private CustomerContact contact;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhotoType type;
    
    @Column(nullable = false)
    private String url;
    
    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;
    
    private String caption;
    
    @ElementCollection
    @CollectionTable(name = "photo_tags")
    private Set<String> tags = new HashSet<>();
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();
    
    // Audit fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;
    
    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;
}
```

## 🏗️ BACKEND SERVICES

### Photo Service

```java
@ApplicationScoped
@Transactional
public class CustomerPhotoService {
    
    @Inject
    CustomerPhotoRepository photoRepository;
    
    @Inject
    S3Service s3Service;
    
    @Inject
    ImageProcessingService imageService;
    
    public PhotoUploadResponse uploadPhoto(
        UUID customerId, 
        MultipartFormDataInput input
    ) {
        // 1. Extract and validate file
        InputStream fileStream = extractFile(input);
        String mimeType = detectMimeType(fileStream);
        
        // 2. Process image (resize, compress)
        ProcessedImage processed = imageService.processImage(
            fileStream, 
            new ImageProcessingOptions()
                .maxWidth(1200)
                .maxHeight(1200)
                .quality(0.85)
        );
        
        // 3. Generate thumbnail
        ProcessedImage thumbnail = imageService.createThumbnail(
            processed.getStream(),
            150 // size
        );
        
        // 4. Upload to S3
        String photoKey = generateS3Key(customerId, "photo");
        String thumbKey = generateS3Key(customerId, "thumb");
        
        String photoUrl = s3Service.upload(photoKey, processed);
        String thumbUrl = s3Service.upload(thumbKey, thumbnail);
        
        // 5. Save metadata to DB
        CustomerPhoto photo = new CustomerPhoto();
        photo.setCustomerId(customerId);
        photo.setUrl(photoUrl);
        photo.setThumbnailUrl(thumbUrl);
        photo.setType(extractType(input));
        photo.setMimeType(mimeType);
        photo.setSizeBytes(processed.getSize());
        photo.setWidth(processed.getWidth());
        photo.setHeight(processed.getHeight());
        
        photoRepository.persist(photo);
        
        return mapper.toResponse(photo);
    }
}
```

### Image Processing

```java
@ApplicationScoped
public class ImageProcessingService {
    
    public ProcessedImage processImage(
        InputStream input,
        ImageProcessingOptions options
    ) {
        try {
            BufferedImage original = ImageIO.read(input);
            
            // Calculate new dimensions
            Dimension newSize = calculateSize(
                original.getWidth(),
                original.getHeight(),
                options.getMaxWidth(),
                options.getMaxHeight()
            );
            
            // Resize if needed
            BufferedImage resized = resize(original, newSize);
            
            // Compress
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(options.getQuality());
            
            writer.setOutput(ImageIO.createImageOutputStream(output));
            writer.write(null, new IIOImage(resized, null, null), params);
            
            return new ProcessedImage(
                output.toByteArray(),
                newSize.width,
                newSize.height
            );
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to process image", e);
        }
    }
}
```

## 🔒 SECURITY & STORAGE

### S3 Configuration

```yaml
# application.properties
quarkus.s3.region=eu-central-1
quarkus.s3.bucket=freshplan-customer-photos
quarkus.s3.access-key-id=${S3_ACCESS_KEY}
quarkus.s3.secret-access-key=${S3_SECRET_KEY}

# S3 Bucket Policy
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::freshplan-customer-photos/public/*"
    }
  ]
}
```

### Storage Strategy

```java
public class S3StorageStrategy {
    // Folder structure:
    // /customers/{customerId}/photos/{photoId}-{size}.jpg
    // /customers/{customerId}/thumbs/{photoId}-thumb.jpg
    
    private String generateS3Key(UUID customerId, String type, UUID photoId) {
        return String.format(
            "customers/%s/%s/%s-%s.jpg",
            customerId,
            type.equals("thumb") ? "thumbs" : "photos",
            photoId,
            type.equals("thumb") ? "thumb" : "full"
        );
    }
}
```

## 🔗 WEITERE DOKUMENTATION

- [Frontend Implementation](/docs/features/ACTIVE/01_security/FC-033_IMPLEMENTATION.md)
- [Test-Strategie](/docs/features/ACTIVE/01_security/FC-033_TESTING.md)
- [Zurück zur Übersicht](/docs/features/ACTIVE/01_security/FC-033_OVERVIEW.md)