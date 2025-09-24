// Users management page
import { Box, Dialog, DialogContent, IconButton } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { UserTableMUI } from '../features/users/UserTableMUI';
import { UserFormMUI } from '../features/users/UserFormMUI';
import { useUser } from '../features/users/userQueries';
import { useUserStore } from '../features/users/userStore';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';

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
    <MainLayoutV2>
      <Box sx={{ p: 3 }}>
        {/* Main content - Always show table */}
        <UserTableMUI />

        {/* Create user dialog */}
        <Dialog
          open={isCreateModalOpen}
          onClose={closeCreateModal}
          maxWidth="md"
          fullWidth
          PaperProps={{
            sx: {
              borderRadius: 2,
              maxHeight: '90vh',
            },
          }}
        >
          <IconButton
            aria-label="close"
            onClick={closeCreateModal}
            sx={{
              position: 'absolute',
              right: 8,
              top: 8,
              color: theme => theme.palette.grey[500],
            }}
          >
            <CloseIcon />
          </IconButton>
          <DialogContent sx={{ p: 4 }}>
            <UserFormMUI onSuccess={handleFormSuccess} onCancel={closeCreateModal} />
          </DialogContent>
        </Dialog>

        {/* Edit user dialog */}
        <Dialog
          open={isEditModalOpen}
          onClose={closeEditModal}
          maxWidth="md"
          fullWidth
          PaperProps={{
            sx: {
              borderRadius: 2,
              maxHeight: '90vh',
            },
          }}
        >
          <IconButton
            aria-label="close"
            onClick={closeEditModal}
            sx={{
              position: 'absolute',
              right: 8,
              top: 8,
              color: theme => theme.palette.grey[500],
            }}
          >
            <CloseIcon />
          </IconButton>
          <DialogContent sx={{ p: 4 }}>
            {selectedUser ? (
              <UserFormMUI
                user={selectedUser}
                onSuccess={handleFormSuccess}
                onCancel={closeEditModal}
              />
            ) : (
              <Box sx={{ textAlign: 'center', p: 4 }}>Lade Benutzerdaten...</Box>
            )}
          </DialogContent>
        </Dialog>
      </Box>
    </MainLayoutV2>
  );
};
