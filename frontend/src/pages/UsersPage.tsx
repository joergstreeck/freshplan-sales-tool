// Users management page
import { UserTable } from '../features/users/UserTable';
import { UserForm } from '../features/users/UserForm';
import { useUser } from '../features/users/userQueries';
import { useUserStore } from '../features/users/userStore';

export const UsersPage = () => {
  const { isCreateModalOpen, isEditModalOpen, selectedUserId, closeCreateModal, closeEditModal } =
    useUserStore();

  // Fetch user data when editing
  const { data: selectedUser } = useUser(selectedUserId || '');

  const handleFormSuccess = () => {
    // Close modals on successful form submission
    closeCreateModal();
    closeEditModal();
  };

  return (
    <div className="min-h-screen bg-background p-8">
      <div className="mx-auto max-w-7xl space-y-8">
        {/* Main content */}
        {!isCreateModalOpen && !isEditModalOpen && <UserTable />}

        {/* Create user form */}
        {isCreateModalOpen && (
          <div className="flex justify-center">
            <UserForm onSuccess={handleFormSuccess} onCancel={closeCreateModal} />
          </div>
        )}

        {/* Edit user form */}
        {isEditModalOpen && selectedUser && (
          <div className="flex justify-center">
            <UserForm user={selectedUser} onSuccess={handleFormSuccess} onCancel={closeEditModal} />
          </div>
        )}

        {/* Loading state for edit mode */}
        {isEditModalOpen && !selectedUser && selectedUserId && (
          <div className="flex justify-center">
            <div className="p-8">
              <p>Lade Benutzerdaten...</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
