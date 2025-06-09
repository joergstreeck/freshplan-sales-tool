import { useState } from 'react';
import { useUsers, useDeleteUser, useToggleUserStatus } from './userQueries';
import { useUserStore } from './userStore';
import { Button } from '../../components/ui/button-transition';
import { Input } from '../../components/ui/input';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '../../components/ui/card-transition';
import type { User, UserFilter } from './userSchemas';

export const UserTable = () => {
  const [searchInput, setSearchInput] = useState('');
  const { activeFilters, setActiveFilters, openEditModal, openCreateModal } = useUserStore();

  // Merge search term into filters
  const filters: UserFilter = {
    ...activeFilters,
    search: searchInput.trim() || undefined,
  };

  const { data: users = [], isLoading, error } = useUsers(filters);
  const deleteUser = useDeleteUser();
  const toggleUserStatus = useToggleUserStatus();

  const handleSearch = () => {
    // Trigger search by updating filters
    setActiveFilters({
      ...activeFilters,
      search: searchInput.trim() || undefined,
    });
  };

  const handleClearSearch = () => {
    setSearchInput('');
    setActiveFilters({
      ...activeFilters,
      search: undefined,
    });
  };

  const handleDeleteUser = async (user: User) => {
    if (window.confirm(`Benutzer "${user.username}" wirklich löschen?`)) {
      try {
        await deleteUser.mutateAsync(user.id);
      } catch (error) {
        console.error('Failed to delete user:', error);
      }
    }
  };

  const handleToggleStatus = async (user: User) => {
    try {
      await toggleUserStatus.mutateAsync({
        id: user.id,
        enabled: !user.enabled,
      });
    } catch (error) {
      console.error('Failed to toggle user status:', error);
    }
  };

  if (error) {
    return (
      <Card>
        <CardContent className="p-6">
          <p className="text-destructive">
            Fehler beim Laden der Benutzer:{' '}
            {error instanceof Error ? error.message : 'Unbekannter Fehler'}
          </p>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      {/* Header with search and create button */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Benutzerverwaltung</CardTitle>
              <CardDescription>Verwalten Sie Benutzer und deren Rollen</CardDescription>
            </div>
            <Button onClick={openCreateModal}>Neuer Benutzer</Button>
          </div>
        </CardHeader>

        <CardContent>
          {/* Search and filters */}
          <div className="flex items-center space-x-2">
            <Input
              placeholder="Benutzer suchen..."
              value={searchInput}
              onChange={e => setSearchInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleSearch()}
              className="max-w-sm"
            />
            <Button onClick={handleSearch} variant="outline">
              Suchen
            </Button>
            {(searchInput || Object.keys(activeFilters).length > 0) && (
              <Button onClick={handleClearSearch} variant="ghost">
                Zurücksetzen
              </Button>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Users table */}
      <Card>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="p-6 text-center">
              <p>Lade Benutzer...</p>
            </div>
          ) : users.length === 0 ? (
            <div className="p-6 text-center">
              <p className="text-muted-foreground">Keine Benutzer gefunden.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="border-b">
                  <tr className="text-left">
                    <th className="p-4 font-medium">Benutzername</th>
                    <th className="p-4 font-medium">Name</th>
                    <th className="p-4 font-medium">E-Mail</th>
                    <th className="p-4 font-medium">Rollen</th>
                    <th className="p-4 font-medium">Status</th>
                    <th className="p-4 font-medium">Aktionen</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map(user => (
                    <tr key={user.id} className="border-b hover:bg-muted/50">
                      <td className="p-4 font-medium">{user.username}</td>
                      <td className="p-4">
                        {user.firstName} {user.lastName}
                      </td>
                      <td className="p-4">{user.email}</td>
                      <td className="p-4">
                        <div className="flex flex-wrap gap-1">
                          {user.roles.map(role => (
                            <span
                              key={role}
                              className="px-2 py-1 text-xs rounded-full bg-primary/10 text-primary"
                            >
                              {role}
                            </span>
                          ))}
                        </div>
                      </td>
                      <td className="p-4">
                        <span
                          className={`px-2 py-1 text-xs rounded-full ${
                            user.enabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                          }`}
                        >
                          {user.enabled ? 'Aktiv' : 'Inaktiv'}
                        </span>
                      </td>
                      <td className="p-4">
                        <div className="flex items-center space-x-2">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => openEditModal(user.id)}
                          >
                            Bearbeiten
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleToggleStatus(user)}
                            disabled={toggleUserStatus.isPending}
                          >
                            {user.enabled ? 'Deaktivieren' : 'Aktivieren'}
                          </Button>
                          <Button
                            size="sm"
                            variant="destructive"
                            onClick={() => handleDeleteUser(user)}
                            disabled={deleteUser.isPending}
                          >
                            Löschen
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
