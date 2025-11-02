/**
 * EntityListHeader - Generic List Header Component
 *
 * Shared component for Customer and Lead list pages.
 * Moved from features/customers/ to features/shared/ for Clean Architecture.
 *
 * @since Sprint 2.1.7.7 - Architecture Cleanup
 */

import { Box, Typography, Button, Chip, useTheme } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { UniversalExportButton } from '../../../components/export';

interface EntityListHeaderProps {
  totalCount: number;
  onAddEntity: () => void;
  title?: string;
  createButtonLabel?: string;
  entityType?: 'customer' | 'lead';
}

export function EntityListHeader({
  totalCount,
  onAddEntity,
  title = 'Einträge',
  createButtonLabel = 'Neuer Eintrag',
  entityType = 'customer',
}: EntityListHeaderProps) {
  const theme = useTheme();

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        mb: 3,
        flexWrap: 'wrap',
        gap: 2,
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <Typography variant="h4" component="h1">
          {title}
        </Typography>
        <Chip label={`${totalCount} gesamt`} color="primary" size="small" />
      </Box>

      <Box sx={{ display: 'flex', gap: 2 }}>
        <UniversalExportButton entity={entityType} filters={{}} variant="outlined" />
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={onAddEntity}
          sx={{
            bgcolor: theme.palette.primary.main,
            '&:hover': {
              bgcolor: theme.palette.primary.dark,
            },
          }}
        >
          {createButtonLabel}
        </Button>
      </Box>
    </Box>
  );
}
