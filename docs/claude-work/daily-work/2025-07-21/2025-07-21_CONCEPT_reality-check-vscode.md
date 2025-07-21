# VS Code Extension: Reality Check

## Konzept
Eine VS Code Extension die in Echtzeit Plan und Code abgleicht.

## Features

### 1. Status Bar Item
```
[Reality Check: FC-008 ✅] [FC-011 ❌ 2 missing]
```

### 2. Code Lens Integration
In CLAUDE_TECH Dateien über Code-Beispielen:
```
📍 Implement in: backend/src/main/java/.../AuthService.java [Create]
@Service
public class AuthService {
```

### 3. Quick Actions
- Rechtsklick auf Feature-Dokument → "Run Reality Check"
- Rechtsklick auf Code-Datei → "Update Plan Reference"

### 4. Problems Panel
```
PROBLEMS
❌ FC-008: AuthService.java referenced but not found
❌ FC-011: Plan outdated - UserRole enum has different values
```

### 5. Auto-Fix Commands
- "Create missing files from plan"
- "Update plan with current file paths"
- "Sync code structure to plan"

## Implementation Sketch

```typescript
// extension.ts
export function activate(context: vscode.ExtensionContext) {
    // Status bar
    const statusBar = vscode.window.createStatusBarItem();
    
    // File watcher
    const watcher = vscode.workspace.createFileSystemWatcher('**/*_CLAUDE_TECH.md');
    
    // On file change
    watcher.onDidChange(async (uri) => {
        const featureCode = extractFeatureCode(uri);
        const result = await runRealityCheck(featureCode);
        updateStatusBar(statusBar, result);
    });
    
    // Code lens provider
    vscode.languages.registerCodeLensProvider(
        { pattern: '**/*_CLAUDE_TECH.md' },
        new RealityCheckCodeLensProvider()
    );
}
```

## Benefits
- Echtzeit-Feedback
- Keine manuellen Checks nötig
- Direkte Navigation Plan ↔ Code
- Auto-Fix Möglichkeiten