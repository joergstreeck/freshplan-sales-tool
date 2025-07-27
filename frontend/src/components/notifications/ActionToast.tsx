import { Box, Button, Typography } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

interface ActionToastProps {
  message: string;
  action?: {
    label: string;
    onClick: () => void;
  };
  icon?: React.ReactNode;
}

export function ActionToast({ message, action, icon }: ActionToastProps) {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
      {icon || <CheckCircleIcon sx={{ color: 'success.main' }} />}
      <Box sx={{ flex: 1 }}>
        <Typography variant="body2">{message}</Typography>
      </Box>
      {action && (
        <Button
          size="small"
          variant="contained"
          onClick={action.onClick}
          sx={{
            bgcolor: 'white',
            color: 'primary.main',
            '&:hover': {
              bgcolor: 'grey.100'
            }
          }}
        >
          {action.label}
        </Button>
      )}
    </Box>
  );
}