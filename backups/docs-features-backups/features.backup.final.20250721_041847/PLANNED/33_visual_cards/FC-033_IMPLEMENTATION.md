# 👁️ FC-033 VISUAL CUSTOMER CARDS - IMPLEMENTATION

[← Zurück zur Übersicht](/docs/features/ACTIVE/01_security/FC-033_OVERVIEW.md)

## 📦 FRONTEND KOMPONENTEN

### Customer Card {#customer-card}

**Enhanced Customer Card mit visuellen Elementen:**

```typescript
// src/features/customers/components/VisualCustomerCard.tsx
import { Card, CardMedia, Box, Avatar, AvatarGroup, Chip } from '@mui/material';
import { Restaurant as RestaurantIcon, LocalShipping as LocalShippingIcon } from '@mui/icons-material';
import { useCustomerPhotos } from '../hooks/useCustomerPhotos';

interface VisualCustomerCardProps {
  customer: Customer;
  onClick?: () => void;
}

export const VisualCustomerCard: React.FC<VisualCustomerCardProps> = ({ 
  customer, 
  onClick 
}) => {
  const { photos, loading } = useCustomerPhotos(customer.id);
  
  return (
    <Card 
      sx={{ maxWidth: 345, cursor: onClick ? 'pointer' : 'default' }}
      onClick={onClick}
    >
      {/* Hero Image - Gebäude oder Logo */}
      <CardMedia
        component="img"
        height="140"
        image={customer.buildingPhoto || '/placeholder-building.jpg'}
        alt={customer.name}
        sx={{ objectFit: 'cover' }}
      />
      
      {/* Contact Avatars */}
      <Box sx={{ p: 2, display: 'flex', gap: 1 }}>
        <AvatarGroup max={3}>
          {customer.contacts.map(contact => (
            <Avatar
              key={contact.id}
              alt={contact.name}
              src={contact.photo}
              sx={{ width: 56, height: 56 }}
            >
              {getInitials(contact.name)}
            </Avatar>
          ))}
        </AvatarGroup>
      </Box>
      
      {/* Visual Notes */}
      <Box sx={{ px: 2, pb: 2 }}>
        <Chip
          size="small"
          icon={<RestaurantIcon />}
          label="Italienisches Restaurant"
        />
        <Chip
          size="small"
          icon={<LocalShippingIcon />}
          label="Lieferung: Hintereingang"
          sx={{ ml: 1 }}
        />
      </Box>
    </Card>
  );
};
```

### Photo Upload {#photo-upload}

**Komponente für Foto-Upload mit Kompression:**

```typescript
// src/features/customers/components/PhotoUpload.tsx
import { useState } from 'react';
import { Button, CircularProgress } from '@mui/material';
import { PhotoCamera as PhotoCameraIcon } from '@mui/icons-material';
import { compressImage } from '../utils/imageCompression';
import { useUploadPhoto } from '../hooks/useUploadPhoto';

interface PhotoUploadProps {
  customerId: string;
  type: 'contact' | 'building' | 'note';
  onSuccess?: (photoUrl: string) => void;
}

export const PhotoUpload: React.FC<PhotoUploadProps> = ({ 
  customerId, 
  type,
  onSuccess 
}) => {
  const [uploading, setUploading] = useState(false);
  const { uploadPhoto } = useUploadPhoto();

  const handleUpload = async (file: File) => {
    if (!file) return;
    
    setUploading(true);
    try {
      // Compress before upload
      const compressed = await compressImage(file, {
        maxWidth: 1200,
        maxHeight: 1200,
        quality: 0.8
      });
      
      const formData = new FormData();
      formData.append('photo', compressed);
      formData.append('type', type);
      
      const result = await uploadPhoto(customerId, formData);
      onSuccess?.(result.url);
    } catch (error) {
      console.error('Upload failed:', error);
    } finally {
      setUploading(false);
    }
  };
  
  return (
    <Button
      component="label"
      variant="outlined"
      startIcon={uploading ? <CircularProgress size={20} /> : <PhotoCameraIcon />}
      disabled={uploading}
    >
      {uploading ? 'Uploading...' : 'Foto hinzufügen'}
      <input
        type="file"
        hidden
        accept="image/*"
        capture="environment"
        onChange={(e) => handleUpload(e.target.files?.[0]!)}
      />
    </Button>
  );
};
```

### Visual Memory Aids

**Eselsbrücken und visuelle Merkhilfen:**

```typescript
// src/features/customers/components/MemoryAids.tsx
import { Box, Typography, Chip, Avatar } from '@mui/material';

interface MemoryAidsProps {
  customer: Customer;
  editable?: boolean;
}

export const MemoryAids: React.FC<MemoryAidsProps> = ({ customer, editable }) => {
  const [aids, setAids] = useState(customer.memoryAids || []);
  
  return (
    <Box sx={{ mt: 2 }}>
      <Typography variant="subtitle2" gutterBottom>
        Eselsbrücken:
      </Typography>
      
      {/* Visual Associations */}
      {aids.map((aid, index) => (
        <Chip
          key={index}
          avatar={<Avatar>{aid.icon}</Avatar>}
          label={aid.text}
          size="small"
          sx={{ mr: 1, mb: 1 }}
          onDelete={editable ? () => removeAid(index) : undefined}
        />
      ))}
      
      {editable && (
        <Chip
          label="+ Hinzufügen"
          size="small"
          onClick={addNewAid}
          sx={{ borderStyle: 'dashed' }}
        />
      )}
    </Box>
  );
};
```

## 🔧 UTILITIES & HOOKS

### Image Compression

```typescript
// src/features/customers/utils/imageCompression.ts
import imageCompression from 'browser-image-compression';

interface CompressionOptions {
  maxWidth: number;
  maxHeight: number;
  quality: number;
}

export const compressImage = async (
  file: File, 
  options: CompressionOptions
): Promise<File> => {
  const compressionOptions = {
    maxSizeMB: 1,
    maxWidthOrHeight: Math.max(options.maxWidth, options.maxHeight),
    useWebWorker: true,
    fileType: 'image/jpeg',
    initialQuality: options.quality
  };
  
  return await imageCompression(file, compressionOptions);
};

// Generate thumbnails
export const createThumbnail = async (
  file: File,
  size: number = 150
): Promise<Blob> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d')!;
        
        // Calculate aspect ratio
        const aspectRatio = img.width / img.height;
        let width = size;
        let height = size;
        
        if (aspectRatio > 1) {
          height = size / aspectRatio;
        } else {
          width = size * aspectRatio;
        }
        
        canvas.width = width;
        canvas.height = height;
        ctx.drawImage(img, 0, 0, width, height);
        
        canvas.toBlob(
          (blob) => blob ? resolve(blob) : reject(new Error('Failed to create thumbnail')),
          'image/jpeg',
          0.7
        );
      };
      img.src = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  });
};
```

### Custom Hooks

```typescript
// src/features/customers/hooks/useCustomerPhotos.ts
import { useQuery } from '@tanstack/react-query';
import { customerApi } from '../api/customerApi';

export const useCustomerPhotos = (customerId: string) => {
  return useQuery({
    queryKey: ['customer-photos', customerId],
    queryFn: () => customerApi.getPhotos(customerId),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// src/features/customers/hooks/useUploadPhoto.ts
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { customerApi } from '../api/customerApi';

export const useUploadPhoto = () => {
  const queryClient = useQueryClient();
  
  const mutation = useMutation({
    mutationFn: ({ customerId, formData }: { customerId: string; formData: FormData }) => 
      customerApi.uploadPhoto(customerId, formData),
    onSuccess: (data, { customerId }) => {
      queryClient.invalidateQueries({ queryKey: ['customer-photos', customerId] });
    },
  });
  
  return {
    uploadPhoto: mutation.mutate,
    isUploading: mutation.isPending,
    error: mutation.error,
  };
};
```

## 📱 MOBILE OPTIMIERUNG

### Responsive Photo Gallery

```typescript
// src/features/customers/components/PhotoGallery.tsx
import { ImageList, ImageListItem, ImageListItemBar, IconButton } from '@mui/material';
import { Edit as EditIcon, Fullscreen as FullscreenIcon } from '@mui/icons-material';
import { useBreakpoint } from '../../../hooks/useBreakpoint';

export const PhotoGallery: React.FC<{ photos: Photo[] }> = ({ photos }) => {
  const { isMobile, isTablet } = useBreakpoint();
  const cols = isMobile ? 2 : isTablet ? 3 : 4;
  
  return (
    <ImageList sx={{ width: '100%', height: 450 }} cols={cols} gap={8}>
      {photos.map((photo) => (
        <ImageListItem key={photo.id}>
          <img
            src={photo.thumbnail}
            alt={photo.caption}
            loading="lazy"
            style={{ cursor: 'pointer' }}
            onClick={() => openLightbox(photo)}
          />
          <ImageListItemBar
            title={photo.caption}
            subtitle={formatDate(photo.date)}
            actionIcon={
              <>
                <IconButton onClick={() => openLightbox(photo)}>
                  <FullscreenIcon />
                </IconButton>
                <IconButton onClick={() => editCaption(photo)}>
                  <EditIcon />
                </IconButton>
              </>
            }
          />
        </ImageListItem>
      ))}
    </ImageList>
  );
};
```

## 🔗 WEITERE DOKUMENTATION

- [API Endpoints & Datenmodell](/docs/features/ACTIVE/01_security/FC-033_API.md)
- [Test-Strategie](/docs/features/ACTIVE/01_security/FC-033_TESTING.md)
- [Zurück zur Übersicht](/docs/features/ACTIVE/01_security/FC-033_OVERVIEW.md)