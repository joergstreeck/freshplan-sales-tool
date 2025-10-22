// App providers setup - React Query + Auth + Router
import { ReactNode, lazy, Suspense } from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, CssBaseline, CircularProgress, Box } from '@mui/material';
import { CacheProvider } from '@emotion/react';
import { Toaster } from 'react-hot-toast';
import { emotionCache } from './emotion-cache';
import { queryClient } from './lib/queryClient';
import { AuthProvider } from './contexts/AuthContext';
import { KeycloakProvider } from './contexts/KeycloakContext';
import { ErrorBoundary } from './components/ErrorBoundary';
import { KeyboardShortcutsProvider } from './hooks/useKeyboardShortcuts';
import { HelpProvider } from './features/help';
import freshfoodzTheme from './theme/freshfoodz';
import App from './App';
import { LoginBypassPage } from './pages/LoginBypassPage';
import { ProtectedRoute } from './components/auth/ProtectedRoute';

// Lazy load heavy pages to reduce initial bundle size
const UsersPage = lazy(() => import('./pages/UsersPage').then(m => ({ default: m.UsersPage })));
const IntegrationTestPage = lazy(() =>
  import('./pages/IntegrationTestPage').then(m => ({ default: m.IntegrationTestPage }))
);
const CustomersPageV2 = lazy(() =>
  import('./pages/CustomersPageV2').then(m => ({ default: m.CustomersPageV2 }))
);
const CockpitPage = lazy(() =>
  import('./pages/CockpitPage').then(m => ({ default: m.CockpitPage }))
);
const CockpitPageV2 = lazy(() =>
  import('./pages/CockpitPageV2').then(m => ({ default: m.CockpitPageV2 }))
);
const OpportunityPipelinePage = lazy(() =>
  import('./pages/OpportunityPipelinePage').then(m => ({ default: m.OpportunityPipelinePage }))
);
const OpportunityDetailPage = lazy(() =>
  import('./pages/OpportunityDetailPage').then(m => ({ default: m.OpportunityDetailPage }))
);
// const HelpSystemDemoPage = lazy(() =>
//   import('./pages/HelpSystemDemoPage').then(m => ({ default: m.HelpSystemDemoPage }))
// );
const AuditAdminPage = lazy(() =>
  import('./pages/admin/AuditAdminPage').then(m => ({ default: m.AuditAdminPage }))
);
const CustomerDetailPage = lazy(() =>
  import('./pages/CustomerDetailPage').then(m => ({ default: m.CustomerDetailPage }))
);
const TestAuditTimeline = lazy(() => import('./pages/TestAuditTimeline'));
const LazyLoadingDemo = lazy(() =>
  import('./pages/LazyLoadingDemo').then(m => ({ default: m.LazyLoadingDemo }))
);
const HelpCenterPage = lazy(() =>
  import('./pages/HelpCenterPage').then(m => ({ default: m.HelpCenterPage }))
);
const ApiStatusPage = lazy(() =>
  import('./pages/ApiStatusPage').then(m => ({ default: m.ApiStatusPage }))
);
const NotFoundPage = lazy(() =>
  import('./pages/NotFoundPage').then(m => ({ default: m.NotFoundPage }))
);
const AdminDashboard = lazy(() =>
  import('./pages/AdminDashboard').then(m => ({ default: m.AdminDashboard }))
);
const SystemDashboard = lazy(() =>
  import('./pages/admin/SystemDashboard').then(m => ({ default: m.SystemDashboard }))
);
const IntegrationsDashboard = lazy(() =>
  import('./pages/admin/IntegrationsDashboard').then(m => ({ default: m.IntegrationsDashboard }))
);
const HelpConfigDashboard = lazy(() =>
  import('./pages/admin/HelpConfigDashboard').then(m => ({ default: m.HelpConfigDashboard }))
);
const HelpSystemDemoPageV2 = lazy(() =>
  import('./pages/HelpSystemDemoPageV2').then(m => ({ default: m.HelpSystemDemoPageV2 }))
);
const OpportunitySettingsPage = lazy(() =>
  import('./pages/admin/OpportunitySettingsPage').then(m => ({
    default: m.OpportunitySettingsPage,
  }))
);
// Dashboards für Hauptmenüpunkte
const NeukundengewinnungDashboard = lazy(() =>
  import('./pages/NeukundengewinnungDashboard').then(m => ({
    default: m.NeukundengewinnungDashboard,
  }))
);
const KundenmanagementDashboard = lazy(() =>
  import('./pages/KundenmanagementDashboard').then(m => ({ default: m.KundenmanagementDashboard }))
);
const AuswertungenDashboard = lazy(() =>
  import('./pages/AuswertungenDashboard').then(m => ({ default: m.AuswertungenDashboard }))
);
const KommunikationDashboard = lazy(() =>
  import('./pages/KommunikationDashboard').then(m => ({ default: m.KommunikationDashboard }))
);
const EinstellungenDashboard = lazy(() =>
  import('./pages/EinstellungenDashboard').then(m => ({ default: m.EinstellungenDashboard }))
);
const HilfeDashboard = lazy(() =>
  import('./pages/HilfeDashboard').then(m => ({ default: m.HilfeDashboard }))
);

// Feature-flagged components
const LeadsPage = lazy(() => import('./pages/LeadsPage'));
const LeadDetailPage = lazy(() =>
  import('./pages/LeadDetailPage').then(m => ({ default: m.LeadDetailPage }))
);

// Lazy load all placeholder pages
import * as Placeholders from './pages/placeholders';

// Loading component for lazy loaded pages
const PageLoader = () => (
  <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
    <CircularProgress />
  </Box>
);

interface AppProvidersProps {
  children?: ReactNode;
}

export const AppProviders = ({ children: mainChildren }: AppProvidersProps) => {
  // Only include login bypass in development mode
  const isDevelopmentMode = import.meta.env.DEV && import.meta.env.MODE !== 'production';

  // Feature flags
  const FEAT_LEADGEN = (import.meta.env.VITE_FEATURE_LEADGEN ?? 'false') === 'true';

  // Auth Provider wrapper - AuthProvider always depends on KeycloakContext
  const AuthWrapper = ({ children: authChildren }: { children: ReactNode }) => {
    return (
      <KeycloakProvider>
        <AuthProvider>{authChildren}</AuthProvider>
      </KeycloakProvider>
    );
  };

  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <CacheProvider value={emotionCache}>
          <ThemeProvider theme={freshfoodzTheme}>
            <CssBaseline />
            <BrowserRouter>
              <AuthWrapper>
                <KeyboardShortcutsProvider>
                  <HelpProvider>
                    <Toaster
                      position="top-right"
                      toastOptions={{
                        duration: 4000,
                        style: {
                          background: '#424242',
                          color: '#fff',
                        },
                        success: {
                          style: {
                            background: '#94C456',
                          },
                        },
                        error: {
                          style: {
                            background: '#EF5350',
                          },
                        },
                      }}
                    />
                    {mainChildren || (
                      <Suspense fallback={<PageLoader />}>
                        <Routes>
                          <Route path="/" element={<App />} />
                          <Route path="/cockpit" element={<CockpitPage />} />
                          <Route path="/cockpit-v2" element={<CockpitPageV2 />} />
                          <Route path="/users" element={<UsersPage />} />
                          <Route path="/customers" element={<CustomersPageV2 />} />
                          <Route
                            path="/customers/new"
                            element={<CustomersPageV2 openWizard={true} />}
                          />
                          <Route path="/customers/:customerId" element={<CustomerDetailPage />} />
                          {/* Customer Management Routes */}
                          <Route
                            path="/customer-management"
                            element={<KundenmanagementDashboard />}
                          />
                          <Route
                            path="/customer-management/opportunities"
                            element={<OpportunityPipelinePage />}
                          />
                          {/* Opportunity Detail Route - Sprint 2.1.7.1 */}
                          <Route path="/opportunities/:id" element={<OpportunityDetailPage />} />
                          <Route
                            path="/customer-management/activities"
                            element={<Placeholders.Aktivitaeten />}
                          />

                          {/* Help Center Routes */}
                          <Route path="/help" element={<HilfeDashboard />} />
                          <Route path="/help/*" element={<HelpCenterPage />} />

                          {/* Admin Routes - Protected by Role */}
                          <Route
                            path="/admin/audit"
                            element={
                              <ProtectedRoute allowedRoles={['admin', 'auditor']}>
                                <AuditAdminPage />
                              </ProtectedRoute>
                            }
                          />
                          {/* Admin User Management */}
                          <Route
                            path="/admin/users"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <UsersPage />
                              </ProtectedRoute>
                            }
                          />

                          {/* Admin System Routes */}
                          <Route
                            path="/admin/system/api-test"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <ApiStatusPage />
                              </ProtectedRoute>
                            }
                          />

                          {/* Admin Help Configuration */}
                          <Route
                            path="/admin/help/demo"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <HelpSystemDemoPageV2 />
                              </ProtectedRoute>
                            }
                          />

                          {/* Placeholder Pages für leere Menüpunkte */}

                          {/* Lead Generation - with own Dashboard */}
                          <Route
                            path="/lead-generation"
                            element={<NeukundengewinnungDashboard />}
                          />
                          <Route
                            path="/lead-generation/inbox"
                            element={<Placeholders.EmailPosteingang />}
                          />
                          <Route
                            path="/lead-generation/leads"
                            element={FEAT_LEADGEN ? <LeadsPage /> : <Placeholders.LeadErfassung />}
                          />
                          {/* Lead Detail Page */}
                          {FEAT_LEADGEN && (
                            <Route
                              path="/lead-generation/leads/:slug"
                              element={<LeadDetailPage />}
                            />
                          )}
                          <Route
                            path="/lead-generation/campaigns"
                            element={<Placeholders.Kampagnen />}
                          />

                          {/* Reports - with own Dashboard */}
                          <Route path="/reports" element={<AuswertungenDashboard />} />
                          <Route path="/reports/revenue" element={<Placeholders.UmsatzBericht />} />
                          <Route
                            path="/reports/customers"
                            element={<Placeholders.KundenAnalyse />}
                          />
                          <Route
                            path="/reports/activities"
                            element={<Placeholders.AktivitaetsberBerichte />}
                          />

                          {/* Communication - with own Dashboard */}
                          <Route path="/communication" element={<KommunikationDashboard />} />
                          <Route path="/communication/chat" element={<Placeholders.TeamChat />} />
                          <Route
                            path="/communication/announcements"
                            element={<Placeholders.Ankuendigungen />}
                          />
                          <Route path="/communication/notes" element={<Placeholders.Notizen />} />
                          <Route
                            path="/communication/messages"
                            element={<Placeholders.InterneNachrichten />}
                          />

                          {/* Settings - with own Dashboard */}
                          <Route path="/settings" element={<EinstellungenDashboard />} />
                          <Route path="/settings/profile" element={<Placeholders.MeinProfil />} />
                          <Route
                            path="/settings/notifications"
                            element={<Placeholders.Benachrichtigungen />}
                          />
                          <Route
                            path="/settings/appearance"
                            element={<Placeholders.Darstellung />}
                          />
                          <Route path="/settings/security" element={<Placeholders.Sicherheit />} />

                          {/* Help - uses HelpCenterPage */}
                          <Route
                            path="/help/getting-started"
                            element={<Placeholders.ErsteSchritte />}
                          />
                          <Route path="/help/manuals" element={<Placeholders.Handbuecher />} />
                          <Route path="/help/videos" element={<Placeholders.VideoTutorials />} />
                          <Route path="/help/faq" element={<Placeholders.FAQ />} />
                          <Route path="/help/support" element={<Placeholders.Support />} />

                          {/* Admin - mit neuem Dashboard */}
                          <Route path="/admin" element={<AdminDashboard />} />
                          <Route path="/admin/system" element={<SystemDashboard />} />
                          <Route path="/admin/integrations" element={<IntegrationsDashboard />} />
                          <Route path="/admin/help" element={<HelpConfigDashboard />} />
                          <Route
                            path="/admin/settings"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <Placeholders.AdminSettings />
                              </ProtectedRoute>
                            }
                          />
                          <Route
                            path="/admin/settings/opportunities"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <OpportunitySettingsPage />
                              </ProtectedRoute>
                            }
                          />
                          <Route
                            path="/admin/system/logs"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <Placeholders.SystemLogs />
                              </ProtectedRoute>
                            }
                          />
                          <Route
                            path="/admin/system/performance"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <Placeholders.Performance />
                              </ProtectedRoute>
                            }
                          />
                          <Route
                            path="/admin/system/backup"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <Placeholders.BackupRecovery />
                              </ProtectedRoute>
                            }
                          />

                          {/* Admin Integrations */}
                          <Route
                            path="/admin/integrations/ai"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <Placeholders.KIAnbindungen />
                              </ProtectedRoute>
                            }
                          />
                          <Route
                            path="/admin/integrations/xentral"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <Placeholders.XentralIntegration />
                              </ProtectedRoute>
                            }
                          />
                          <Route
                            path="/admin/integrations/email"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <Placeholders.EmailServices />
                              </ProtectedRoute>
                            }
                          />
                          <Route
                            path="/admin/integrations/payment"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <Placeholders.PaymentProvider />
                              </ProtectedRoute>
                            }
                          />
                          <Route
                            path="/admin/integrations/webhooks"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <Placeholders.Webhooks />
                              </ProtectedRoute>
                            }
                          />
                          <Route
                            path="/admin/integrations/new"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <Placeholders.NeueIntegration />
                              </ProtectedRoute>
                            }
                          />
                          <Route
                            path="/admin/reports"
                            element={
                              <ProtectedRoute allowedRoles={['admin', 'auditor', 'manager']}>
                                <Placeholders.ComplianceReports />
                              </ProtectedRoute>
                            }
                          />
                          <Route
                            path="/admin/help/tooltips"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <Placeholders.TooltipsVerwalten />
                              </ProtectedRoute>
                            }
                          />
                          <Route
                            path="/admin/help/tours"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <Placeholders.TourenErstellen />
                              </ProtectedRoute>
                            }
                          />
                          <Route
                            path="/admin/help/analytics"
                            element={
                              <ProtectedRoute allowedRoles={['admin']}>
                                <Placeholders.HelpAnalytics />
                              </ProtectedRoute>
                            }
                          />

                          {/* Login Bypass temporär reaktiviert - Auto-Login Problem */}
                          {isDevelopmentMode && (
                            <Route path="/login-bypass" element={<LoginBypassPage />} />
                          )}
                          {isDevelopmentMode && (
                            <Route path="/integration-test" element={<IntegrationTestPage />} />
                          )}
                          {isDevelopmentMode && (
                            <Route path="/test-audit-timeline" element={<TestAuditTimeline />} />
                          )}
                          {isDevelopmentMode && (
                            <Route path="/lazy-loading-demo" element={<LazyLoadingDemo />} />
                          )}

                          {/* Redirect alte URLs auf neue */}
                          <Route path="/kunden" element={<Navigate to="/customers" replace />} />
                          <Route
                            path="/verkaufschancen"
                            element={<Navigate to="/customer-management/opportunities" replace />}
                          />
                          {/* Redirects für alte deutsche URLs */}
                          <Route
                            path="/neukundengewinnung"
                            element={<Navigate to="/lead-generation" replace />}
                          />
                          <Route
                            path="/kundenmanagement"
                            element={<Navigate to="/customer-management" replace />}
                          />
                          <Route path="/berichte" element={<Navigate to="/reports" replace />} />
                          <Route
                            path="/kommunikation"
                            element={<Navigate to="/communication" replace />}
                          />
                          <Route
                            path="/einstellungen"
                            element={<Navigate to="/settings" replace />}
                          />
                          <Route path="/hilfe" element={<Navigate to="/help" replace />} />

                          {/* System-Alias-Routen */}
                          <Route path="/system" element={<Navigate to="/admin" replace />} />
                          <Route
                            path="/system/api-status"
                            element={<Navigate to="/admin/system/api-test" replace />}
                          />
                          <Route
                            path="/system/help"
                            element={<Navigate to="/admin/help/demo" replace />}
                          />

                          {/* 404 Fallback - muss am Ende stehen */}
                          <Route path="*" element={<NotFoundPage />} />
                        </Routes>
                      </Suspense>
                    )}
                  </HelpProvider>
                </KeyboardShortcutsProvider>
              </AuthWrapper>
            </BrowserRouter>

            {/* React Query DevTools - only in development */}
            {import.meta.env.DEV && (
              <ReactQueryDevtools initialIsOpen={false} buttonPosition="bottom-right" />
            )}
          </ThemeProvider>
        </CacheProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
};
