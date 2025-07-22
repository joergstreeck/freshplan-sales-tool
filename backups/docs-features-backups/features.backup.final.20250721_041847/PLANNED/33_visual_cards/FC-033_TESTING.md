# 👁️ FC-033 VISUAL CUSTOMER CARDS - TESTING

[← Zurück zur Übersicht](/docs/features/ACTIVE/01_security/FC-033_OVERVIEW.md) | [← API Dokumentation](/docs/features/ACTIVE/01_security/FC-033_API.md)

---

## 🧪 TEST-STRATEGIE

### Test-Coverage-Ziele
- **Unit Tests:** 85% Coverage
- **Integration Tests:** Alle API Endpoints
- **E2E Tests:** Happy Path Szenarien
- **Performance Tests:** Upload & Display Zeit

---

## 🔍 UNIT TESTS

### Frontend Component Tests

```typescript
// VisualCustomerCard.test.tsx
import { render, screen } from '@testing-library/react';
import { VisualCustomerCard } from './VisualCustomerCard';

describe('VisualCustomerCard', () => {
  const mockCustomer = {
    id: '123',
    name: 'Restaurant Milano',
    buildingPhoto: 'https://example.com/building.jpg',
    contacts: [
      { id: '1', name: 'Max Müller', photo: 'https://example.com/max.jpg' }
    ]
  };

  it('zeigt Gebäudebild an', () => {
    render(<VisualCustomerCard customer={mockCustomer} />);
    const img = screen.getByAltText('Restaurant Milano');
    expect(img).toHaveAttribute('src', mockCustomer.buildingPhoto);
  });

  it('zeigt Placeholder wenn kein Bild vorhanden', () => {
    const customerNoPhoto = { ...mockCustomer, buildingPhoto: null };
    render(<VisualCustomerCard customer={customerNoPhoto} />);
    const img = screen.getByAltText('Restaurant Milano');
    expect(img).toHaveAttribute('src', '/placeholder-building.jpg');
  });

  it('zeigt Kontakt-Avatare an', () => {
    render(<VisualCustomerCard customer={mockCustomer} />);
    expect(screen.getByText('MM')).toBeInTheDocument(); // Initials
  });
});
```

### Upload Service Tests

```typescript
// photoUpload.test.ts
import { compressImage } from './photoUpload';

describe('Photo Upload Service', () => {
  it('komprimiert Bilder korrekt', async () => {
    const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    const compressed = await compressImage(file, {
      maxSizeMB: 1,
      maxWidthOrHeight: 1200
    });
    
    expect(compressed.size).toBeLessThan(file.size);
    expect(compressed.type).toBe('image/jpeg');
  });

  it('behält Aspect Ratio bei', async () => {
    // Mock implementation test
    const dimensions = calculateDimensions(1920, 1080, 1200);
    expect(dimensions.width).toBe(1200);
    expect(dimensions.height).toBe(675); // Maintains 16:9
  });
});
```

---

## 🔌 INTEGRATION TESTS

### API Endpoint Tests

```java
// PhotoResourceTest.java
@QuarkusTest
class PhotoResourceTest {
    
    @Test
    void testPhotoUpload() {
        given()
            .auth().oauth2(getValidToken())
            .multiPart("photo", new File("test.jpg"))
            .multiPart("type", "building")
            .when()
            .post("/api/customers/{id}/photos", TEST_CUSTOMER_ID)
            .then()
            .statusCode(201)
            .body("url", notNullValue())
            .body("thumbnailUrl", notNullValue())
            .body("type", equalTo("building"));
    }
    
    @Test
    void testGetCustomerPhotos() {
        given()
            .auth().oauth2(getValidToken())
            .queryParam("type", "contact")
            .when()
            .get("/api/customers/{id}/photos", TEST_CUSTOMER_ID)
            .then()
            .statusCode(200)
            .body("photos", hasSize(greaterThan(0)))
            .body("photos[0].type", equalTo("contact"));
    }
    
    @Test
    void testUnauthorizedAccess() {
        given()
            .when()
            .get("/api/customers/{id}/photos", TEST_CUSTOMER_ID)
            .then()
            .statusCode(401);
    }
}
```

---

## 🎭 E2E TESTS

### Visual Upload Flow

```typescript
// e2e/visualCards.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Visual Customer Cards', () => {
  test('Foto-Upload Workflow', async ({ page }) => {
    // Login
    await page.goto('/login');
    await login(page, 'test@example.com', 'password');
    
    // Navigate to customer
    await page.goto('/customers/123');
    
    // Upload building photo
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles('tests/fixtures/building.jpg');
    
    // Wait for upload
    await expect(page.locator('.MuiAlert-root')).toContainText('Erfolgreich hochgeladen');
    
    // Verify photo appears
    await expect(page.locator('img[alt*="building"]')).toBeVisible();
  });
  
  test('Foto-Galerie Navigation', async ({ page }) => {
    await page.goto('/customers/123/photos');
    
    // Click thumbnail
    await page.locator('.MuiImageListItem-root').first().click();
    
    // Lightbox should open
    await expect(page.locator('.lightbox')).toBeVisible();
    
    // Navigate with arrows
    await page.keyboard.press('ArrowRight');
    await expect(page.locator('.lightbox img')).toHaveAttribute('src', /photo2/);
  });
});
```

---

## ⚡ PERFORMANCE TESTS

### Upload Performance

```typescript
// performance/uploadPerf.test.ts
test('Foto-Upload Performance', async () => {
  const times = [];
  
  for (let i = 0; i < 10; i++) {
    const start = performance.now();
    await uploadPhoto(testFile);
    const end = performance.now();
    times.push(end - start);
  }
  
  const avgTime = times.reduce((a, b) => a + b) / times.length;
  expect(avgTime).toBeLessThan(3000); // < 3 seconds average
});
```

### Load Time Tests

```javascript
// lighthouse.config.js
module.exports = {
  ci: {
    collect: {
      url: ['http://localhost:3000/customers/123'],
      settings: {
        preset: 'desktop',
        throttling: {
          cpuSlowdownMultiplier: 1
        }
      }
    },
    assert: {
      assertions: {
        'first-contentful-paint': ['error', { maxNumericValue: 2000 }],
        'largest-contentful-paint': ['error', { maxNumericValue: 3000 }],
        'cumulative-layout-shift': ['error', { maxNumericValue: 0.1 }]
      }
    }
  }
};
```

---

## 🐛 EDGE CASES & ERROR HANDLING

### Test Cases

1. **Große Dateien:** > 10MB Upload
2. **Falsche Formate:** PDF statt Bild
3. **Netzwerk-Fehler:** Während Upload
4. **Speicher voll:** S3 Quota erreicht
5. **Korrupte Bilder:** Ungültige EXIF-Daten

### Error Handling Tests

```typescript
test('zeigt Fehler bei ungültigem Format', async () => {
  const pdfFile = new File(['pdf'], 'doc.pdf', { type: 'application/pdf' });
  
  const result = await uploadPhoto(pdfFile);
  
  expect(result.error).toBe('Nur Bildformate erlaubt (JPG, PNG, WEBP)');
});

test('zeigt Fehler bei zu großer Datei', async () => {
  const bigFile = createMockFile(15 * 1024 * 1024); // 15MB
  
  const result = await uploadPhoto(bigFile);
  
  expect(result.error).toBe('Maximale Dateigröße: 10MB');
});
```

---

## 📊 TEST-METRIKEN

### Erwartete Ergebnisse
- **Upload-Zeit:** < 3s für 5MB Bild
- **Thumbnail-Generation:** < 500ms
- **Gallery-Load:** < 1s für 20 Bilder
- **Memory-Usage:** < 100MB für Gallery
- **Offline-Cache:** 95% Hit Rate