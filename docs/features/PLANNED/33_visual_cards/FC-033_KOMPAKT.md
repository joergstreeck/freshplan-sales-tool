# 👁️ FC-033 VISUAL CUSTOMER CARDS (ARCHIVIERT)

**HINWEIS:** Dieses Dokument wurde aufgeteilt in:
- [FC-033_OVERVIEW.md](./FC-033_OVERVIEW.md) - Übersicht & Business Value
- [FC-033_IMPLEMENTATION.md](./FC-033_IMPLEMENTATION.md) - Frontend & Backend Code
- [FC-033_API.md](./FC-033_API.md) - API & Datenmodell
- [FC-033_TESTING.md](./FC-033_TESTING.md) - Test-Strategie

---

# 👁️ FC-033 VISUAL CUSTOMER CARDS (KOMPAKT)

**Erstellt:** 19.07.2025  
**Status:** 📋 READY TO START  
**Feature-Typ:** 🎨 FRONTEND  
**Priorität:** MEDIUM - Nice to have, aber WOW-Effekt  
**Geschätzt:** 1 Tag  

---

## 🧠 WAS WIR BAUEN

**Problem:** Namen vergessen, Gesichter merken  
**Lösung:** Kunden mit Bildern verknüpfen  
**Value:** Persönlichere Beziehungen  

> **Business Case:** "Ach, Sie sind Herr Müller!" → Vertrauen

### 🎯 Visual Features:
- **Kundenfotos:** Ansprechpartner mit Gesicht
- **Gebäudebilder:** Restaurant, Eingang, Küche
- **Visuelle Notizen:** Handskizzen, Logos
- **Produkt-Favoriten:** Was bestellt er gerne?

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **Enhanced Customer Card:**
```typescript
const VisualCustomerCard = ({ customer }) => {
  const photos = useCustomerPhotos(customer.id);
  
  return (
    <Card sx={{ maxWidth: 345 }}>
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

### 2. **Photo Upload & Gallery:**
```typescript
const PhotoUpload = ({ customerId, type }) => {
  const handleUpload = async (file: File) => {
    // Compress before upload
    const compressed = await compressImage(file, {
      maxWidth: 1200,
      maxHeight: 1200,
      quality: 0.8
    });
    
    const formData = new FormData();
    formData.append('photo', compressed);
    formData.append('type', type); // 'contact', 'building', 'note'
    
    await api.uploadCustomerPhoto(customerId, formData);
  };
  
  return (
    <Button
      component="label"
      variant="outlined"
      startIcon={<PhotoCameraIcon />}
    >
      Foto hinzufügen
      <input
        type="file"
        hidden
        accept="image/*"
        capture="environment"
        onChange={(e) => handleUpload(e.target.files[0])}
      />
    </Button>
  );
};
```

### 3. **Visual Memory Aids:**
```typescript
const MemoryAids = ({ customer }) => (
  <Box sx={{ mt: 2 }}>
    <Typography variant="subtitle2" gutterBottom>
      Eselsbrücken:
    </Typography>
    
    {/* Visual Associations */}
    <Chip
      avatar={<Avatar>👨‍🍳</Avatar>}
      label="Koch heißt auch Koch"
      size="small"
    />
    <Chip
      avatar={<Avatar>🏛️</Avatar>}
      label="Altbau mit Stuck"
      size="small"
      sx={{ ml: 1 }}
    />
    <Chip
      avatar={<Avatar>🚗</Avatar>}
      label="Fährt roten Porsche"
      size="small"
      sx={{ ml: 1 }}
    />
  </Box>
);
```

---

## 📸 SMART FEATURES

### Face Recognition (Optional):
```typescript
// Mit face-api.js
const detectFaces = async (imageUrl: string) => {
  await faceapi.nets.ssdMobilenetv1.loadFromUri('/models');
  
  const img = await faceapi.fetchImage(imageUrl);
  const detections = await faceapi.detectAllFaces(img);
  
  // Auto-crop Gesichter für Kontakt-Avatare
  return detections.map(detection => {
    const box = detection.box;
    return cropImage(img, box);
  });
};
```

### Visual Search:
```typescript
// "Zeige alle Restaurants mit rotem Logo"
const visualSearch = {
  byColor: async (color: string) => {
    const customers = await db.customers.toArray();
    return customers.filter(c => 
      c.logoColor?.includes(color) || 
      c.visualTags?.includes(color)
    );
  },
  
  byType: async (type: string) => {
    // "Altbau", "Modern", "Kette"
    return db.customers
      .where('buildingType')
      .equals(type)
      .toArray();
  }
};
```

---

## 🎨 UI COMPONENTS

### Photo Gallery:
```typescript
const PhotoGallery = ({ photos }) => (
  <ImageList sx={{ width: '100%', height: 450 }} cols={3}>
    {photos.map((photo) => (
      <ImageListItem key={photo.id}>
        <img
          src={photo.thumbnail}
          alt={photo.caption}
          loading="lazy"
          onClick={() => openLightbox(photo)}
        />
        <ImageListItemBar
          title={photo.caption}
          subtitle={formatDate(photo.date)}
          actionIcon={
            <IconButton onClick={() => editCaption(photo)}>
              <EditIcon />
            </IconButton>
          }
        />
      </ImageListItem>
    ))}
  </ImageList>
);
```

### Visual Timeline:
```typescript
// Zeige Bilder in der Timeline
const VisualTimelineItem = ({ event }) => {
  if (event.type === 'photo_added') {
    return (
      <TimelineItem>
        <TimelineSeparator>
          <TimelineDot color="primary">
            <PhotoIcon />
          </TimelineDot>
        </TimelineSeparator>
        <TimelineContent>
          <Card>
            <CardMedia
              component="img"
              height="200"
              image={event.photo.url}
            />
            <CardContent>
              <Typography variant="caption">
                {event.user} hat Foto hinzugefügt
              </Typography>
            </CardContent>
          </Card>
        </TimelineContent>
      </TimelineItem>
    );
  }
};
```

---

## 🔧 TECHNICAL DETAILS

### Image Optimization:
```typescript
const imageOptimizer = {
  // Client-side compression
  compress: async (file: File) => {
    const options = {
      maxSizeMB: 1,
      maxWidthOrHeight: 1920,
      useWebWorker: true
    };
    
    return await imageCompression(file, options);
  },
  
  // Generate thumbnails
  createThumbnail: async (file: File) => {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    canvas.width = 150;
    canvas.height = 150;
    
    // ... resize logic
    
    return canvas.toBlob(blob => blob, 'image/jpeg', 0.7);
  }
};
```

### Storage Strategy:
```typescript
// Hybrid: S3 + Local Cache
const photoStorage = {
  upload: async (photo: Blob) => {
    // 1. Upload to S3
    const url = await s3.upload(photo);
    
    // 2. Cache locally for offline
    await caches.open('photos').then(cache => {
      cache.put(url, new Response(photo));
    });
    
    return url;
  },
  
  get: async (url: string) => {
    // 1. Try cache first
    const cached = await caches.match(url);
    if (cached) return cached;
    
    // 2. Fetch and cache
    const response = await fetch(url);
    const cache = await caches.open('photos');
    cache.put(url, response.clone());
    
    return response;
  }
};
```

---

## 🎯 SUCCESS METRICS

- **Adoption:** 60% nutzen Fotos
- **Recognition:** Namen 90% schneller erinnert
- **Engagement:** +40% App-Nutzung
- **Storage:** Avg 5 Fotos/Kunde

**Integration:** Tag 12-13 mit File Management!

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[📁 FC-024 File Management](/docs/features/PLANNED/24_file_management/FC-024_KOMPAKT.md)** - Foto-Speicherung
- **[👥 M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_KOMPAKT.md)** - Customer Data Model
- **[📱 FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_KOMPAKT.md)** - Kamera-Integration

### ⚡ Bild-Quellen & Integration:
- **[📈 FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_KOMPAKT.md)** - Foto-Activities
- **[📱 FC-018 Mobile PWA](/docs/features/PLANNED/18_mobile_pwa/FC-018_KOMPAKT.md)** - Offline-Fotos
- **[🔄 FC-032 Offline-First](/docs/features/PLANNED/32_offline_first/FC-032_KOMPAKT.md)** - Foto-Sync

### 🚀 Ermöglicht folgende Features:
- **[🔍 FC-034 Instant Insights](/docs/features/PLANNED/34_instant_insights/FC-034_KOMPAKT.md)** - Visual Recognition
- **[📊 FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_KOMPAKT.md)** - Foto-Engagement
- **[🎯 FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_KOMPAKT.md)** - Foto-Erinnerungen

### 🎨 UI Integration:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Visual Cards Display
- **[⚡ M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md)** - Foto-Upload
- **[🧭 M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md)** - Galerie-View

### 🔧 Technische Details:
- **FC-033_IMPLEMENTATION_GUIDE.md** *(geplant)* - Visual Cards Setup
- **FC-033_DECISION_LOG.md** *(geplant)* - Storage-Entscheidungen
- **PHOTO_GUIDELINES.md** *(geplant)* - Best Practices für Fotos