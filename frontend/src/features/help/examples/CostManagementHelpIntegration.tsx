import React from 'react';
import { Box, Typography, Card, CardContent, Button } from '@mui/material';
import { HelpTooltip, useHelp } from '../index';
import { CustomerFieldThemeProvider } from '../../customers/theme/CustomerFieldThemeProvider';

/**
 * Beispiel-Integration des Help Systems in das Cost Management Feature
 * 
 * Zeigt Best Practices für:
 * - HelpTooltip Integration
 * - useHelp Hook Verwendung
 * - Theme Provider Wrapping
 * - Contextual Help Loading
 */
export const CostManagementExample: React.FC = () => {
  // Help Hook für Cost Management Feature
  const { 
    showHelp, 
    showTour, 
    hasHelp,
    context 
  } = useHelp({ 
    feature: 'cost-management',
    autoLoad: true // Lädt Help Content automatisch
  });
  
  return (
    <CustomerFieldThemeProvider mode="anpassungsfähig">
      <Card>
        <CardContent>
          <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
            <Box display="flex" alignItems="center">
              <Typography variant="h6">
                Cost Management System
              </Typography>
              {/* Help Tooltip direkt neben dem Titel */}
              <HelpTooltip feature="cost-management" />
            </Box>
            
            {/* Tour Button wenn verfügbar */}
            {hasHelp && (
              <Button 
                size="small" 
                variant="outlined"
                onClick={showTour}
              >
                Feature Tour
              </Button>
            )}
          </Box>
          
          {/* Zeige User-Level Context */}
          {context && (
            <Typography variant="caption" color="text.secondary" display="block" mb={2}>
              Hilfe angepasst für: {context.userLevel} Level
              {context.isFirstTime && ' (Erste Nutzung)'}
            </Typography>
          )}
          
          {/* Budget Limit Section mit eigener Hilfe */}
          <Box mb={3}>
            <Box display="flex" alignItems="center">
              <Typography variant="subtitle1">
                Monatliches Budget-Limit
              </Typography>
              <HelpTooltip feature="cost-management-budget" />
            </Box>
            <Typography variant="body2" color="text.secondary">
              Aktuell: €500 / Monat
            </Typography>
          </Box>
          
          {/* Service Costs Section */}
          <Box>
            <Box display="flex" alignItems="center">
              <Typography variant="subtitle1">
                Externe Service Kosten
              </Typography>
              <HelpTooltip feature="cost-management-services" />
            </Box>
            <Typography variant="body2" color="text.secondary">
              OpenAI API: €0.02/1k tokens
            </Typography>
          </Box>
          
          {/* Detailed Help Button */}
          <Box mt={3}>
            <Button 
              variant="contained" 
              color="primary"
              onClick={() => showHelp()}
              fullWidth
            >
              Detaillierte Hilfe anzeigen
            </Button>
          </Box>
        </CardContent>
      </Card>
    </CustomerFieldThemeProvider>
  );
};

/**
 * Best Practices für Help Integration:
 * 
 * 1. IMMER CustomerFieldThemeProvider verwenden (MANDATORY!)
 * 2. HelpTooltip für inline Kontext-Hilfe
 * 3. useHelp Hook für programmatische Kontrolle
 * 4. Feature-spezifische Help IDs verwenden
 * 5. autoLoad für wichtige Features aktivieren
 * 6. Tour Button prominent platzieren
 * 7. User Level Context anzeigen für Transparenz
 */

// Beispiel für Struggle Detection Integration
export const FormWithStruggleDetection: React.FC = () => {
  const [errors, setErrors] = React.useState(0);
  // Struggle detection is handled automatically by HelpProvider
  
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    // Simuliere Validation Error
    if (errors < 3) {
      setErrors(errors + 1);
      
      // Report struggle after 3 failed attempts
      if (errors >= 2) {
        // This will trigger ProactiveHelp
        console.log('User struggling with form');
      }
    }
  };
  
  return (
    <form onSubmit={handleSubmit}>
      {/* Form fields */}
      <Button type="submit">Submit</Button>
    </form>
  );
};