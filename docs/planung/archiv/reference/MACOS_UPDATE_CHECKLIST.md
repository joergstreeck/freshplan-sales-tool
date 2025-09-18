# macOS Update Checklist

## Pre-Update (Already Done ✅)

### Frontend
- ✅ All changes committed (commit: 8040f74)
- ✅ Frontend processes stopped (Vite dev server)
- ✅ Node modules list backed up to `frontend/node_modules_backup.txt`
- ✅ MSW integration added for offline development
- ✅ Integration test infrastructure ready

### Backend (Team BACK verantwortlich)
- Backend-spezifische Schutzmaßnahmen werden von Team BACK durchgeführt

## Post-Update Checklist

### System Requirements
1. **Verify macOS Version**
   ```bash
   sw_vers -productVersion
   ```

2. **Check Development Tools**
   ```bash
   # Node.js
   node --version  # Should be v18 or higher
   
   # npm
   npm --version   # Should be v9 or higher
   
   # Java
   java --version  # Should be Java 17
   
   # Git
   git --version
   ```

### Frontend Recovery

1. **Navigate to Frontend**
   ```bash
   cd /Users/joergstreeck/freshplan-sales-tool/frontend
   ```

2. **Reinstall Dependencies**
   ```bash
   # Clean install
   rm -rf node_modules package-lock.json
   npm install
   ```

3. **Verify Installation**
   ```bash
   # Check for missing packages
   npm ls --depth=0
   
   # Run tests
   npm test
   ```

4. **Start Development Server**
   ```bash
   npm run dev
   ```

5. **Test Key Features**
   - http://localhost:5173 - Main App
   - http://localhost:5173/calculator - Calculator
   - http://localhost:5173/users - User Management
   - http://localhost:5173/integration-test - API Tests

### Backend Recovery

1. **Install Docker Desktop** (if not already)
   - Download: https://www.docker.com/products/docker-desktop/

2. **Start Infrastructure**
   ```bash
   cd /Users/joergstreeck/freshplan-sales-tool/infrastructure
   ./start-local-env.sh
   ```

3. **Start Backend**
   ```bash
   cd /Users/joergstreeck/freshplan-sales-tool/backend
   ~/apache-maven-3.9.6/bin/mvn quarkus:dev
   ```

4. **Verify Backend**
   ```bash
   cd /Users/joergstreeck/freshplan-sales-tool/frontend
   node test-backend-integration.js
   ```

### Potential Issues & Solutions

#### Issue: npm command not found
```bash
# Install Node.js via official installer or nvm
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 20
nvm use 20
```

#### Issue: Port already in use
```bash
# Find process using port
lsof -i :5173  # Frontend
lsof -i :8080  # Backend
lsof -i :5432  # PostgreSQL

# Kill process
kill -9 <PID>
```

#### Issue: Permission errors
```bash
# Fix npm permissions
sudo chown -R $(whoami) ~/.npm
sudo chown -R $(whoami) /usr/local/lib/node_modules
```

### Final Verification

Run all tests:
```bash
cd /Users/joergstreeck/freshplan-sales-tool/frontend
npm run lint
npm test
npm run build
```

If all tests pass, the system is ready for development! 🚀

## Important Files Locations

- **Frontend Config**: `frontend/package.json`
- **Backend Config**: `backend/pom.xml`
- **Docker Config**: `infrastructure/docker-compose.yml`
- **Environment**: No `.env` files needed (all configs in code)
- **MSW Mocks**: `frontend/src/mocks/`
- **Integration Tests**: `frontend/test-backend-integration.js`

## Support

If issues persist:
1. Check TEAM_SYNC_LOG.md for latest updates
2. Review BACKEND_START_GUIDE.md for backend setup
3. Contact Team BACK for backend-specific issues