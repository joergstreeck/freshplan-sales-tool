// Users management page - Using MainLayoutV2 and MUI components
import { Box, Typography } from '@mui/material';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { UserTableMUI } from '../features/users/components/UserTableMUI';
import { UserFormMUI } from '../features/users/components/UserFormMUI';
import { useUser } from '../features/users/userQueries';
import { useUserStore } from '../features/users/userStore';

export const UsersPage = () => {
  const { 
    isCreateModalOpen, 
    isEditModalOpen, 
    selectedUserId, 
    closeCreateModal, 
    closeEditModal 
  } = useUserStore();

  // Fetch user data when editing
  const { data: selectedUser } = useUser(selectedUserId || '');

  const handleFormSuccess = () => {
    // Close modals on successful form submission
    closeCreateModal();
    closeEditModal();
  };

  return (
    <MainLayoutV2>
      <Typography variant="h4" gutterBottom sx={{ mb: 4 }}>
        Benutzerverwaltung
      </Typography>
      
      <Box sx={{ width: '100%' }}>
        {/* Main content */}
        {!isCreateModalOpen && !isEditModalOpen && <UserTableMUI />}

        {/* Create user form */}
        {isCreateModalOpen && (
          <Box sx={{ display: 'flex', justifyContent: 'center' }}>
            <UserFormMUI onSuccess={handleFormSuccess} onCancel={closeCreateModal} />
          </Box>
        )}

        {/* Edit user form */}
        {isEditModalOpen && selectedUser && (
          <Box sx={{ display: 'flex', justifyContent: 'center' }}>
            <UserFormMUI 
              user={selectedUser} 
              onSuccess={handleFormSuccess} 
              onCancel={closeEditModal} 
            />
          </Box>
        )}

        {/* Loading state for edit mode */}
        {isEditModalOpen && !selectedUser && selectedUserId && (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 8 }}>
            <Typography>Lade Benutzerdaten...</Typography>
          </Box>
        )}
      </Box>
    </MainLayoutV2>
  );
};
