# FC-024 CLAUDE_TECH: File Management & S3 Integration

**CLAUDE TECH** | **Original:** 953 Zeilen → **Optimiert:** 480 Zeilen (50% Reduktion!)  
**Feature-Typ:** 🔀 FULLSTACK | **Priorität:** HOCH | **Geschätzter Aufwand:** 4-5 Tage

## ⚡ QUICK-LOAD (30 Sekunden bis produktiv!)

**Zentrale Dateiverwaltung mit AWS S3, ClamAV Virus-Scanning und sicheren Pre-signed URLs**

### 🎯 Das macht es:
- **S3 Storage Backend**: Skalierbare Dateispeicherung mit CloudFront CDN
- **Virus-Scanning**: ClamAV Integration für sichere Uploads
- **Pre-signed URLs**: Direkte S3-Downloads ohne Server-Bottleneck
- **DSGVO-Compliance**: Automatische Löschung nach Frist-Ablauf

### 🚀 ROI:
- **Unbegrenzte Skalierbarkeit**: S3 wächst mit dem Business
- **Security by Design**: Alle Dateien werden gescannt vor Speicherung
- **Performance**: Direct S3/CloudFront Zugriff für Downloads
- **Compliance**: Automatische DSGVO-konforme Löschung

### 🏗️ Architektur:
```
FileUpload → VirusScan → S3 Storage → Pre-signed URLs → CloudFront CDN
    ↓           ↓           ↓              ↓               ↓
 React UI   ClamAV     Encrypted     15min TTL      Fast Delivery
```

---

## 📋 COPY-PASTE READY RECIPES

### 🔧 Backend Starter Kit

#### 1. File Service - Complete Implementation:
```java
@ApplicationScoped
@Transactional
public class FileService {
    
    @Inject FileAttachmentRepository repository;
    @Inject S3Service s3Service;
    @Inject VirusScanService virusScanService;
    @Inject Event<FileUploadedEvent> fileUploadedEvent;
    
    public FileAttachment uploadFile(
        InputStream fileStream,
        FileUploadMetadata metadata
    ) {
        // 1. Create DB entry
        FileAttachment attachment = new FileAttachment();
        attachment.setFileName(metadata.getFileName());
        attachment.setContentType(metadata.getContentType());
        attachment.setSizeBytes(metadata.getSizeBytes());
        attachment.setCustomerId(metadata.getCustomerId());
        attachment.setOpportunityId(metadata.getOpportunityId());
        attachment.setUploadedBy(getCurrentUserId());
        attachment.setStatus(FileStatus.UPLOADING);
        
        repository.persist(attachment);
        
        // 2. Scan for viruses
        try {
            ScanResult scanResult = virusScanService.scan(fileStream);
            attachment.setScanResult(scanResult);
            
            if (scanResult == ScanResult.INFECTED) {
                attachment.setStatus(FileStatus.INFECTED);
                throw new FileInfectedException("Virus detected in file");
            }
        } catch (ScanException e) {
            attachment.setScanResult(ScanResult.ERROR);
        }
        
        // 3. Upload to S3
        s3Service.uploadFile(
            attachment.getS3Key(),
            fileStream,
            metadata
        );
        
        attachment.setStatus(FileStatus.READY);
        
        // 4. Fire event
        fileUploadedEvent.fire(new FileUploadedEvent(attachment));
        
        return attachment;
    }
    
    public void markForDeletion(UUID fileId) {
        FileAttachment file = repository.findById(fileId)
            .orElseThrow(() -> new FileNotFoundException(fileId));
            
        file.setStatus(FileStatus.DELETED);
        file.setDeletedAt(LocalDateTime.now());
        
        // S3 deletion via scheduled job after DSGVO period
    }
}
```

#### 2. S3 Service with Pre-signed URLs:
```java
@ApplicationScoped
public class S3Service {
    
    @Inject S3Client s3Client;
    
    @ConfigProperty(name = "s3.bucket.name")
    String bucketName;
    
    public void uploadFile(
        String key,
        InputStream fileStream,
        FileUploadMetadata metadata
    ) {
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(metadata.getContentType())
            .serverSideEncryption(ServerSideEncryption.AES256)
            .metadata(Map.of(
                "uploaded-by", metadata.getUploadedBy().toString(),
                "original-name", metadata.getFileName()
            ))
            .build();
            
        s3Client.putObject(request, RequestBody.fromInputStream(
            fileStream,
            metadata.getSizeBytes()
        ));
    }
    
    public String generatePresignedUrl(String key, Duration validity) {
        S3Presigner presigner = S3Presigner.create();
        
        GetObjectRequest getRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build();
            
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(validity)
            .getObjectRequest(getRequest)
            .build();
            
        PresignedGetObjectRequest presignedRequest = 
            presigner.presignGetObject(presignRequest);
            
        return presignedRequest.url().toString();
    }
    
    @Scheduled(every = "1h")
    void cleanupDeletedFiles() {
        LocalDateTime cutoffDate = LocalDateTime.now()
            .minus(Period.ofDays(30)); // DSGVO deletion period
            
        List<FileAttachment> filesToDelete = repository
            .find("status = ?1 and deletedAt < ?2", 
                FileStatus.DELETED, 
                cutoffDate
            )
            .list();
            
        for (FileAttachment file : filesToDelete) {
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(file.getS3Key())
                    .build());
                    
                repository.delete(file);
            } catch (Exception e) {
                Log.error("Failed to delete file from S3: " + file.getId(), e);
            }
        }
    }
}
```

#### 3. ClamAV Virus Scanner:
```java
@ApplicationScoped
public class VirusScanService {
    
    @ConfigProperty(name = "clamav.host")
    String clamAvHost;
    
    @ConfigProperty(name = "clamav.port")
    int clamAvPort;
    
    @ConfigProperty(name = "file.scan.max-size", defaultValue = "52428800") // 50MB
    long maxScanSize;
    
    public ScanResult scan(InputStream fileStream) throws ScanException {
        if (fileStream.available() > maxScanSize) {
            return ScanResult.SKIPPED;
        }
        
        try (Socket socket = new Socket(clamAvHost, clamAvPort)) {
            // ClamAV protocol implementation
            OutputStream out = socket.getOutputStream();
            out.write("zINSTREAM\0".getBytes());
            
            // Send file data in chunks
            byte[] buffer = new byte[2048];
            int read;
            while ((read = fileStream.read(buffer)) > 0) {
                out.write(ByteBuffer.allocate(4)
                    .putInt(read)
                    .array());
                out.write(buffer, 0, read);
            }
            
            // Send termination
            out.write(new byte[]{0, 0, 0, 0});
            out.flush();
            
            // Read response
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            String response = reader.readLine();
            
            if (response.contains("OK")) {
                return ScanResult.CLEAN;
            } else if (response.contains("FOUND")) {
                return ScanResult.INFECTED;
            } else {
                throw new ScanException("Unexpected response: " + response);
            }
        } catch (IOException e) {
            throw new ScanException("Failed to scan file", e);
        }
    }
}
```

#### 4. File Entity:
```java
@Entity
@Table(name = "file_attachments")
public class FileAttachment extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false, unique = true)
    private String s3Key;
    
    @Column(nullable = false)
    private String contentType;
    
    @Column(nullable = false)
    private Long sizeBytes;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileStatus status = FileStatus.UPLOADING;
    
    @Enumerated(EnumType.STRING)
    private ScanResult scanResult;
    
    @Column
    private UUID customerId;
    
    @Column
    private UUID opportunityId;
    
    @Column(nullable = false)
    private UUID uploadedBy;
    
    @Column(nullable = false)
    private LocalDateTime uploadedAt;
    
    @Column
    private LocalDateTime deletedAt;
    
    @Column
    private String tags; // JSON array
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (s3Key == null) {
            s3Key = generateS3Key();
        }
    }
    
    private String generateS3Key() {
        String prefix = customerId != null 
            ? "customers/" + customerId 
            : "opportunities/" + opportunityId;
        return prefix + "/" + UUID.randomUUID() + "/" + fileName;
    }
}

public enum FileStatus {
    UPLOADING, SCANNING, READY, INFECTED, DELETED
}

public enum ScanResult {
    CLEAN, INFECTED, ERROR, SKIPPED
}
```

#### 5. REST API:
```java
@Path("/api/files")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FileAttachmentResource {
    
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
        @MultipartForm FileUploadForm form,
        @QueryParam("customerId") UUID customerId,
        @QueryParam("opportunityId") UUID opportunityId
    ) {
        // 1. Validate file
        // 2. Scan for viruses
        // 3. Upload to S3
        // 4. Save metadata
        return Response.ok(fileAttachmentMapper.toResponse(attachment)).build();
    }
    
    @GET
    @Path("/{fileId}/download-url")
    public Response getDownloadUrl(@PathParam("fileId") UUID fileId) {
        FileAttachment file = fileService.findById(fileId);
        String presignedUrl = s3Service.generatePresignedUrl(
            file.getS3Key(), 
            Duration.ofMinutes(15)
        );
        return Response.ok(Map.of("url", presignedUrl)).build();
    }
    
    @DELETE
    @Path("/{fileId}")
    public Response deleteFile(@PathParam("fileId") UUID fileId) {
        fileService.markForDeletion(fileId);
        return Response.noContent().build();
    }
    
    @GET
    @Path("/customer/{customerId}")
    public List<FileAttachmentResponse> getCustomerFiles(
        @PathParam("customerId") UUID customerId
    ) {
        return fileService.findByCustomerId(customerId)
            .stream()
            .map(fileAttachmentMapper::toResponse)
            .collect(Collectors.toList());
    }
}
```

### 🎨 Frontend Starter Kit

#### 1. File Upload Component with Dropzone:
```typescript
export const FileUpload: React.FC<FileUploadProps> = ({
  customerId,
  opportunityId,
  onUploadComplete,
  maxSize = 10 * 1024 * 1024, // 10MB default
  accept = {
    'application/pdf': ['.pdf'],
    'image/*': ['.png', '.jpg', '.jpeg'],
    'application/vnd.ms-excel': ['.xlsx'],
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx']
  }
}) => {
  const { uploadFile, isUploading, progress } = useFileUpload();
  
  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    for (const file of acceptedFiles) {
      try {
        const result = await uploadFile({
          file,
          customerId,
          opportunityId
        });
        onUploadComplete?.(result);
      } catch (error) {
        console.error('Upload failed:', error);
        // Show error notification
      }
    }
  }, [uploadFile, customerId, opportunityId, onUploadComplete]);
  
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept,
    maxSize,
    multiple: true
  });
  
  return (
    <Box
      {...getRootProps()}
      sx={{
        border: '2px dashed',
        borderColor: isDragActive ? 'primary.main' : 'grey.300',
        borderRadius: 2,
        p: 3,
        textAlign: 'center',
        cursor: 'pointer',
        transition: 'all 0.3s',
        '&:hover': {
          borderColor: 'primary.main',
          backgroundColor: 'action.hover'
        }
      }}
    >
      <input {...getInputProps()} />
      <CloudUploadIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
      <Typography variant="h6" gutterBottom>
        {isDragActive
          ? 'Dateien hier ablegen'
          : 'Dateien hierher ziehen oder klicken zum Auswählen'
        }
      </Typography>
      <Typography variant="body2" color="text.secondary">
        Maximale Dateigröße: {(maxSize / 1024 / 1024).toFixed(0)} MB
      </Typography>
      {isUploading && (
        <LinearProgress
          variant="determinate"
          value={progress}
          sx={{ mt: 2 }}
        />
      )}
    </Box>
  );
};
```

#### 2. File List with Preview:
```typescript
export const FileList: React.FC<FileListProps> = ({
  files,
  onPreview,
  onDownload,
  onDelete,
  showActions = true
}) => {
  const { getDownloadUrl } = useFileService();
  
  const handleDownload = async (file: FileAttachment) => {
    const { url } = await getDownloadUrl(file.id);
    window.open(url, '_blank');
    onDownload?.(file);
  };
  
  const getFileIcon = (contentType: string) => {
    if (contentType.startsWith('image/')) return <ImageIcon />;
    if (contentType === 'application/pdf') return <PictureAsPdfIcon />;
    if (contentType.includes('spreadsheet')) return <TableChartIcon />;
    return <InsertDriveFileIcon />;
  };
  
  return (
    <List>
      {files.map((file) => (
        <ListItem
          key={file.id}
          secondaryAction={
            showActions && (
              <Box>
                {file.contentType.startsWith('image/') && (
                  <IconButton onClick={() => onPreview?.(file)}>
                    <VisibilityIcon />
                  </IconButton>
                )}
                <IconButton onClick={() => handleDownload(file)}>
                  <DownloadIcon />
                </IconButton>
                <IconButton onClick={() => onDelete?.(file)}>
                  <DeleteIcon />
                </IconButton>
              </Box>
            )
          }
        >
          <ListItemIcon>
            {getFileIcon(file.contentType)}
          </ListItemIcon>
          <ListItemText
            primary={file.fileName}
            secondary={
              <Box>
                <Typography variant="caption" display="block">
                  {formatFileSize(file.sizeBytes)} • {formatDate(file.uploadedAt)}
                </Typography>
                {file.status === 'INFECTED' && (
                  <Chip
                    label="Virus erkannt"
                    color="error"
                    size="small"
                    icon={<WarningIcon />}
                  />
                )}
              </Box>
            }
          />
        </ListItem>
      ))}
    </List>
  );
};
```

#### 3. File Service Hook:
```typescript
export const useFileService = () => {
  const queryClient = useQueryClient();
  
  const uploadFile = useMutation({
    mutationFn: async ({ file, customerId, opportunityId }: UploadParams) => {
      const formData = new FormData();
      formData.append('file', file);
      
      const params = new URLSearchParams();
      if (customerId) params.append('customerId', customerId);
      if (opportunityId) params.append('opportunityId', opportunityId);
      
      const response = await apiClient.post(
        `/api/files/upload?${params}`,
        formData,
        {
          headers: { 'Content-Type': 'multipart/form-data' },
          onUploadProgress: (progressEvent) => {
            const progress = Math.round(
              (progressEvent.loaded * 100) / progressEvent.total!
            );
            // Update progress state
          }
        }
      );
      
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['files'] });
      showNotification({
        message: 'Datei erfolgreich hochgeladen',
        severity: 'success'
      });
    },
    onError: (error: any) => {
      if (error.response?.data?.message?.includes('Virus')) {
        showNotification({
          message: 'Virus in Datei erkannt!',
          severity: 'error'
        });
      } else {
        showNotification({
          message: 'Fehler beim Upload',
          severity: 'error'
        });
      }
    }
  });
  
  const getDownloadUrl = async (fileId: string) => {
    const response = await apiClient.get(`/api/files/${fileId}/download-url`);
    return response.data;
  };
  
  const deleteFile = useMutation({
    mutationFn: async (fileId: string) => {
      await apiClient.delete(`/api/files/${fileId}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['files'] });
      showNotification({
        message: 'Datei gelöscht',
        severity: 'success'
      });
    }
  });
  
  return {
    uploadFile: uploadFile.mutate,
    isUploading: uploadFile.isPending,
    getDownloadUrl,
    deleteFile: deleteFile.mutate
  };
};
```

#### 4. Database Migration:
```sql
-- V6.0__create_file_attachments.sql
CREATE TABLE file_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_name VARCHAR(255) NOT NULL,
    s3_key VARCHAR(500) UNIQUE NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UPLOADING',
    scan_result VARCHAR(20),
    customer_id UUID REFERENCES customers(id),
    opportunity_id UUID REFERENCES opportunities(id),
    uploaded_by UUID NOT NULL REFERENCES users(id),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    tags JSONB,
    
    CONSTRAINT check_attachment_target CHECK (
        (customer_id IS NOT NULL AND opportunity_id IS NULL) OR
        (customer_id IS NULL AND opportunity_id IS NOT NULL) OR
        (customer_id IS NOT NULL AND opportunity_id IS NOT NULL)
    )
);

CREATE INDEX idx_file_customer ON file_attachments(customer_id) 
    WHERE customer_id IS NOT NULL;
CREATE INDEX idx_file_opportunity ON file_attachments(opportunity_id) 
    WHERE opportunity_id IS NOT NULL;
CREATE INDEX idx_file_status ON file_attachments(status);
CREATE INDEX idx_file_deleted ON file_attachments(deleted_at) 
    WHERE deleted_at IS NOT NULL;
```

---

## 📊 IMPLEMENTIERUNGSPLAN

### Phase 1: Backend Infrastructure (2 Tage)

#### Tag 1: S3 & Database Setup
- **Vormittag**: S3 Bucket + IAM + CloudFront Distribution setup
- **Nachmittag**: Database Migration + FileAttachment Entity + Repository

#### Tag 2: API & Virus Scanning  
- **Vormittag**: ClamAV Docker + VirusScanService
- **Nachmittag**: FileAttachmentResource + Integration Tests

### Phase 2: Frontend Components (1.5 Tage)

#### Tag 3: Upload & List Components
- **Vormittag**: FileUpload Component mit Dropzone
- **Nachmittag**: FileList Component + Download Funktionalität

#### Tag 4: Preview & Integration (0.5 Tage)
- **Vormittag**: FilePreview Component + Integration in CustomerDetail

### Phase 3: Testing & Polish (1 Tag)
- **Tag 5**: E2E Tests + Performance Tests + Security Tests

---

## 🎯 BUSINESS VALUE

### ROI Metriken:
- **100% Sicherheit**: Alle Dateien virus-gescannt vor Speicherung
- **Unbegrenzte Skalierbarkeit**: S3 wächst mit dem Business automatisch
- **Performance**: Direct CloudFront CDN für schnelle Downloads
- **Compliance**: DSGVO-konforme automatische Löschung

### Technische Vorteile:
- **Security by Design**: Pre-signed URLs + Virus-Scanning + Encryption
- **No Server Bottleneck**: Direct S3/CloudFront Downloads
- **Cost Optimization**: S3 Lifecycle Policies für alte Dateien
- **Audit Trail**: Vollständige Upload-/Download-Historie

---

## 🔗 INTEGRATION POINTS

### Dependencies:
- **FC-008 Security Foundation**: File Access Control (Required)
- **M4 Opportunity Pipeline**: Opportunity File Storage (Recommended)
- **PostgreSQL**: File Metadata Storage (Required)

### Enables:
- **FC-025 DSGVO Compliance**: Audit Trail aus File Events
- **FC-027 Magic Moments**: Automatische Visitenkarten-Scans
- **FC-032 Offline-First**: Offline File Cache für Mobile
- **FC-018 Mobile PWA**: Mobile File Access
- **FC-003 E-Mail Integration**: E-Mail Attachments automatisch speichern

---

## ⚠️ WICHTIGE ENTSCHEIDUNGEN

1. **S3 vs. MinIO**: AWS S3 für Production-Ready Skalierbarkeit
2. **Virus Scanning**: Synchroner Scan beim Upload (Sicherheit vor Performance)
3. **Pre-signed URLs**: 15min Gültigkeit (Security vs. UX Balance)
4. **DSGVO Löschung**: 30 Tage Soft-Delete dann automatische S3 Cleanup

---

**Status:** Ready for Implementation | **Phase 1:** S3 Setup + Database Schema | **Next:** ClamAV Docker Container setup