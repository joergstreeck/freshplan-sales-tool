# 📎 FC-024 FILE MANAGEMENT & S3 (KOMPAKT)

**Erstellt:** 18.07.2025  
**Status:** 📋 READY TO START  
**Feature-Typ:** 🔀 FULLSTACK  
**Priorität:** HIGH - Infrastruktur  
**Geschätzt:** 2 Tage  

---

## 🧠 WAS WIR BAUEN

**Problem:** Keine zentrale Dateiverwaltung  
**Lösung:** S3 Integration mit Virus Scan  
**Value:** Sichere Dokumente & Attachments  

> **Business Case:** Verträge, Angebote, Visitenkarten zentral und sicher

### 🎯 Core Features:
- **S3 Storage:** Skalierbare Ablage
- **Virus Scanning:** ClamAV Integration
- **File Preview:** PDFs, Bilder direkt
- **Metadata:** Tags, Kunde, Opportunity

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **S3 Config:**
```properties
# application.properties
s3.bucket.name=freshplan-files
s3.region=eu-central-1
s3.access.key=${AWS_ACCESS_KEY}
s3.secret.key=${AWS_SECRET_KEY}
```

### 2. **File Entity:**
```java
@Entity
public class FileAttachment {
    UUID id;
    String fileName;
    String s3Key;
    String contentType;
    Long sizeBytes;
    FileStatus status; // UPLOADING, SCANNING, READY, INFECTED
    UUID customerId;
    UUID opportunityId;
}
```

### 3. **Upload Endpoint:**
```java
@POST
@Path("/files/upload")
@Consumes(MediaType.MULTIPART_FORM_DATA)
public Response upload(
    @MultipartForm FileUpload file
) {
    // 1. Virus scan
    // 2. S3 upload
    // 3. Save metadata
}
```

---

## 🛡️ SECURITY FEATURES

### Virus Scanning:
```java
@ApplicationScoped
public class VirusScanService {
    
    @ConfigProperty(name = "clamav.host")
    String clamAvHost;
    
    public ScanResult scan(InputStream file) {
        // ClamAV scan via REST API
        // CLEAN, INFECTED, ERROR
    }
    
    @Async
    public CompletionStage<ScanResult> 
    scanAsync(String s3Key) {
        // Background scanning
    }
}
```

### Secure URLs:
```java
// Pre-signed URLs (15min gültig)
public String getDownloadUrl(UUID fileId) {
    FileAttachment file = find(fileId);
    
    return s3Client.utilities()
        .getUrl(GetUrlRequest.builder()
            .bucket(bucket)
            .key(file.s3Key)
            .build())
        .toString() + "?expires=900";
}
```

---

## 🎨 FRONTEND COMPONENTS

### File Upload:
```typescript
<FileDropzone
  accept={{
    'application/pdf': ['.pdf'],
    'image/*': ['.png', '.jpg'],
    'application/vnd.ms-excel': ['.xlsx']
  }}
  maxSize={10 * 1024 * 1024} // 10MB
  onDrop={handleUpload}
>
  <CloudUploadIcon />
  <Typography>
    Dateien hierher ziehen
  </Typography>
</FileDropzone>
```

### File List:
```typescript
<FileList
  files={attachments}
  onPreview={(file) => {
    if (file.contentType.startsWith('image/')) {
      showImagePreview(file);
    } else if (file.contentType === 'application/pdf') {
      showPdfViewer(file);
    }
  }}
  onDownload={downloadFile}
  onDelete={deleteFile}
/>
```

---

## 📊 DATABASE SCHEMA

```sql
CREATE TABLE file_attachments (
    id UUID PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    s3_key VARCHAR(500) UNIQUE NOT NULL,
    content_type VARCHAR(100),
    size_bytes BIGINT,
    status VARCHAR(20) DEFAULT 'UPLOADING',
    scan_result VARCHAR(20),
    customer_id UUID,
    opportunity_id UUID,
    uploaded_by UUID NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    
    INDEX idx_customer (customer_id),
    INDEX idx_opportunity (opportunity_id)
);
```

---

## 🔧 INFRASTRUCTURE

### S3 Bucket Policy:
```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": {"AWS": "arn:aws:iam::..."},
    "Action": ["s3:GetObject", "s3:PutObject"],
    "Resource": "arn:aws:s3:::freshplan-files/*"
  }]
}
```

### File Organization:
```
freshplan-files/
├── customers/{customerId}/
│   ├── documents/
│   ├── contracts/
│   └── images/
└── opportunities/{opportunityId}/
    ├── proposals/
    └── attachments/
```

---

## 📞 NÄCHSTE SCHRITTE

1. **S3 Client Setup** - AWS SDK
2. **File Upload API** - Multipart
3. **Virus Scan Integration** - ClamAV
4. **File Preview** - Frontend
5. **Pre-signed URLs** - Security
6. **Cleanup Job** - Alte Dateien

**WICHTIG:** DSGVO-konform mit Löschung!