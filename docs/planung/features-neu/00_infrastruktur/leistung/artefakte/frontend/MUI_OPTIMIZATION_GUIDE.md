# MUI Optimization Guide (FreshFoodz)

**Last Updated:** 2025-09-21

## Prinzipien
- **Tree-Shaking**: Nur benötigte Komponenten importieren (`@mui/material/Button` statt Barrel `@mui/material`).
- **Icons**: Einzelimporte (`@mui/icons-material/Close`). Für Top-Icons Inline-SVG.
- **Theme V2**: CSS Variables nutzen; keine Farbcodes hardcoden.
- **Styles**: `sx` bevorzugen, emotion-cache mit `prepend: true` einsetzen.

## Imports – richtig vs. falsch
```ts
// ✅ Richtig
import Button from '@mui/material/Button'
import TextField from '@mui/material/TextField'
import CloseIcon from '@mui/icons-material/Close'

// ❌ Falsch (zieht zu viel)
import { Button, TextField } from '@mui/material'
import * as Icons from '@mui/icons-material'
```

## Performance
- `CssBaseline` nur einmal in der App-Shell.
- Große Komponenten (DataGrid, RichEditor) **lazy** laden.
- `@mui/x-data-grid` nur dort importieren, wo wirklich gebraucht.
