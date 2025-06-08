import { useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';

/**
 * Test-only login bypass page for development.
 * This should ONLY be included in development builds!
 */
export function LoginBypassPage() {
  const navigate = useNavigate();

  const mockLogin = (role: string) => {
    // Create mock JWT token based on role
    const mockTokens: Record<string, string> = {
      admin: 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Im1vY2sta2V5LWlkIn0.eyJleHAiOjE5OTk5OTk5OTksImlhdCI6MTYwOTQ1OTIwMCwianRpIjoibW9jay1qdGktYWRtaW4iLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgxODAvcmVhbG1zL2ZyZXNocGxhbiIsImF1ZCI6ImZyZXNocGxhbi1iYWNrZW5kIiwic3ViIjoibW9jay1hZG1pbi11c2VyIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZnJlc2hwbGFuLWJhY2tlbmQiLCJzZXNzaW9uX3N0YXRlIjoibW9jay1zZXNzaW9uIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImFkbWluIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiZnJlc2hwbGFuLWJhY2tlbmQiOnsicm9sZXMiOlsiYWRtaW4iXX19LCJzY29wZSI6Im9wZW5pZCBlbWFpbCBwcm9maWxlIiwic2lkIjoibW9jay1zaWQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IkFkbWluIFVzZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhZG1pbiIsImdpdmVuX25hbWUiOiJBZG1pbiIsImZhbWlseV9uYW1lIjoiVXNlciIsImVtYWlsIjoiYWRtaW5AZnJlc2hwbGFuLmRlIn0.mock-signature',
      manager: 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Im1vY2sta2V5LWlkIn0.eyJleHAiOjE5OTk5OTk5OTksImlhdCI6MTYwOTQ1OTIwMCwianRpIjoibW9jay1qdGktbWFuYWdlciIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODE4MC9yZWFsbXMvZnJlc2hwbGFuIiwiYXVkIjoiZnJlc2hwbGFuLWJhY2tlbmQiLCJzdWIiOiJtb2NrLW1hbmFnZXItdXNlciIsInR5cCI6IkJlYXJlciIsImF6cCI6ImZyZXNocGxhbi1iYWNrZW5kIiwic2Vzc2lvbl9zdGF0ZSI6Im1vY2stc2Vzc2lvbiIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJtYW5hZ2VyIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiZnJlc2hwbGFuLWJhY2tlbmQiOnsicm9sZXMiOlsibWFuYWdlciJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUiLCJzaWQiOiJtb2NrLXNpZCIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoiTWFuYWdlciBVc2VyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoibWFuYWdlciIsImdpdmVuX25hbWUiOiJNYW5hZ2VyIiwiZmFtaWx5X25hbWUiOiJVc2VyIiwiZW1haWwiOiJtYW5hZ2VyQGZyZXNocGxhbi5kZSJ9.mock-signature',
      sales: 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Im1vY2sta2V5LWlkIn0.eyJleHAiOjE5OTk5OTk5OTksImlhdCI6MTYwOTQ1OTIwMCwianRpIjoibW9jay1qdGktc2FsZXMiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgxODAvcmVhbG1zL2ZyZXNocGxhbiIsImF1ZCI6ImZyZXNocGxhbi1iYWNrZW5kIiwic3ViIjoibW9jay1zYWxlcy11c2VyIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZnJlc2hwbGFuLWJhY2tlbmQiLCJzZXNzaW9uX3N0YXRlIjoibW9jay1zZXNzaW9uIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbInNhbGVzIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiZnJlc2hwbGFuLWJhY2tlbmQiOnsicm9sZXMiOlsic2FsZXMiXX19LCJzY29wZSI6Im9wZW5pZCBlbWFpbCBwcm9maWxlIiwic2lkIjoibW9jay1zaWQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IlNhbGVzIFVzZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJzYWxlcyIsImdpdmVuX25hbWUiOiJTYWxlcyIsImZhbWlseV9uYW1lIjoiVXNlciIsImVtYWlsIjoic2FsZXNAZnJlc2hwbGFuLmRlIn0.mock-signature',
      viewer: 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Im1vY2sta2V5LWlkIn0.eyJleHAiOjE5OTk5OTk5OTksImlhdCI6MTYwOTQ1OTIwMCwianRpIjoibW9jay1qdGktdmlld2VyIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MTgwL3JlYWxtcy9mcmVzaHBsYW4iLCJhdWQiOiJmcmVzaHBsYW4tYmFja2VuZCIsInN1YiI6Im1vY2stdmlld2VyLXVzZXIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJmcmVzaHBsYW4tYmFja2VuZCIsInNlc3Npb25fc3RhdGUiOiJtb2NrLXNlc3Npb24iLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsidmlld2VyIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiZnJlc2hwbGFuLWJhY2tlbmQiOnsicm9sZXMiOlsidmlld2VyIl19fSwic2NvcGUiOiJvcGVuaWQgZW1haWwgcHJvZmlsZSIsInNpZCI6Im1vY2stc2lkIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJWaWV3ZXIgVXNlciIsInByZWZlcnJlZF91c2VybmFtZSI6InZpZXdlciIsImdpdmVuX25hbWUiOiJWaWV3ZXIiLCJmYW1pbHlfbmFtZSI6IlVzZXIiLCJlbWFpbCI6InZpZXdlckBmcmVzaHBsYW4uZGUifQ.mock-signature'
    };

    const mockUsers = {
      admin: { id: 'mock-admin-user', username: 'admin', email: 'admin@freshplan.de', roles: ['admin'] },
      manager: { id: 'mock-manager-user', username: 'manager', email: 'manager@freshplan.de', roles: ['manager'] },
      sales: { id: 'mock-sales-user', username: 'sales', email: 'sales@freshplan.de', roles: ['sales'] },
      viewer: { id: 'mock-viewer-user', username: 'viewer', email: 'viewer@freshplan.de', roles: ['viewer'] }
    };

    // Store in localStorage for API client
    localStorage.setItem('auth-token', mockTokens[role]);
    localStorage.setItem('auth-user', JSON.stringify(mockUsers[role]));

    // Reload to apply auth context
    window.location.href = '/';
  };

  return (
    <div className="min-h-screen bg-background p-8">
      <div className="mx-auto max-w-2xl">
        <Card>
          <CardHeader>
            <CardTitle>Development Login Bypass</CardTitle>
            <CardDescription>
              Choose a role to login with mock credentials. This is only available in development mode.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-2">
              <Button onClick={() => mockLogin('admin')} variant="default">
                Login as Admin
              </Button>
              <Button onClick={() => mockLogin('manager')} variant="secondary">
                Login as Manager
              </Button>
              <Button onClick={() => mockLogin('sales')} variant="secondary">
                Login as Sales
              </Button>
              <Button onClick={() => mockLogin('viewer')} variant="outline">
                Login as Viewer
              </Button>
            </div>
            <div className="mt-4 text-sm text-muted-foreground">
              <p>Roles:</p>
              <ul className="list-disc list-inside mt-2">
                <li><strong>Admin</strong>: Full access to all features</li>
                <li><strong>Manager</strong>: Can manage users and view reports</li>
                <li><strong>Sales</strong>: Can create and manage orders</li>
                <li><strong>Viewer</strong>: Read-only access</li>
              </ul>
            </div>
            <Button
              onClick={() => navigate('/')}
              variant="ghost"
              className="mt-4 w-full"
            >
              Back to Home
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
