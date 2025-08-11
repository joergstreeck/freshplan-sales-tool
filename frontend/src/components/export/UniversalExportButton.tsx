import React, { useState } from 'react';
import { Button } from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import DescriptionIcon from '@mui/icons-material/Description';
import TableChartIcon from '@mui/icons-material/TableChart';
import CodeIcon from '@mui/icons-material/Code';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import CircularProgress from '@mui/material/CircularProgress';
import { toast } from 'react-hot-toast';

export type ExportFormat = 'csv' | 'excel' | 'json' | 'html' | 'pdf';

interface ExportOption {
  format: ExportFormat;
  label: string;
  icon: React.ReactElement;
  mimeType: string;
  extension: string;
}

interface UniversalExportButtonProps {
  entity: string; // z.B. 'customers', 'audit'
  queryParams?: Record<string, string | number | boolean | undefined>; // Optionale Filter-Parameter
  buttonLabel?: string;
  buttonVariant?: 'text' | 'outlined' | 'contained';
  buttonColor?: 'primary' | 'secondary' | 'success' | 'error' | 'info' | 'warning';
  onExportStart?: () => void;
  onExportComplete?: (format: ExportFormat) => void;
  onExportError?: (error: Error) => void;
  disabled?: boolean;
  className?: string;
}

const exportOptions: ExportOption[] = [
  {
    format: 'csv',
    label: 'CSV (Excel-kompatibel)',
    icon: <TableChartIcon />,
    mimeType: 'text/csv',
    extension: '.csv',
  },
  {
    format: 'excel',
    label: 'Excel (XLSX)',
    icon: <DescriptionIcon />,
    mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    extension: '.xlsx',
  },
  {
    format: 'json',
    label: 'JSON (Datenformat)',
    icon: <CodeIcon />,
    mimeType: 'application/json',
    extension: '.json',
  },
  {
    format: 'html',
    label: 'HTML (Webseite)',
    icon: <DescriptionIcon />,
    mimeType: 'text/html',
    extension: '.html',
  },
  {
    format: 'pdf',
    label: 'PDF (Druckversion)',
    icon: <PictureAsPdfIcon />,
    mimeType: 'application/pdf',
    extension: '.pdf',
  },
];

export const UniversalExportButton: React.FC<UniversalExportButtonProps> = ({
  entity,
  queryParams = {},
  buttonLabel = 'Exportieren',
  buttonVariant = 'outlined',
  buttonColor = 'primary',
  onExportStart,
  onExportComplete,
  onExportError,
  disabled = false,
  className,
}) => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [exporting, setExporting] = useState(false);
  const [exportingFormat, setExportingFormat] = useState<ExportFormat | null>(null);
  const open = Boolean(anchorEl);

  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleExport = async (option: ExportOption) => {
    handleClose();
    setExporting(true);
    setExportingFormat(option.format);

    if (onExportStart) {
      onExportStart();
    }

    try {
      // Build query string
      const queryString = new URLSearchParams(queryParams).toString();
      const url = `/api/v2/export/${entity}/${option.format}${queryString ? `?${queryString}` : ''}`;

      // Fetch the export
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          Accept: option.mimeType,
        },
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        throw new Error(errorData?.error || `Export failed: ${response.statusText}`);
      }

      // Get the filename from Content-Disposition header or generate one
      const contentDisposition = response.headers.get('Content-Disposition');
      let filename = `${entity}_export_${new Date().toISOString().split('T')[0]}${option.extension}`;

      if (contentDisposition) {
        const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
        if (filenameMatch && filenameMatch[1]) {
          filename = filenameMatch[1].replace(/['"]/g, '');
        }
      }

      // Download the file
      const blob = await response.blob();
      const downloadUrl = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(downloadUrl);

      // Success notification
      toast.success(`Export als ${option.label} erfolgreich heruntergeladen!`);

      if (onExportComplete) {
        onExportComplete(option.format);
      }
    } catch (error) {
      console.error('Export error:', error);
      const errorMessage = error instanceof Error ? error.message : 'Export fehlgeschlagen';
      toast.error(errorMessage);

      if (onExportError) {
        onExportError(error instanceof Error ? error : new Error(errorMessage));
      }
    } finally {
      setExporting(false);
      setExportingFormat(null);
    }
  };

  return (
    <>
      <Button
        className={className}
        variant={buttonVariant}
        color={buttonColor}
        startIcon={exporting ? <CircularProgress size={16} /> : <DownloadIcon />}
        onClick={handleClick}
        disabled={disabled || exporting}
        sx={
          buttonColor === 'primary'
            ? {
                borderColor: '#94C456',
                color: '#004F7B',
                '&:hover': {
                  borderColor: '#7AA348',
                  backgroundColor: 'rgba(148, 196, 86, 0.08)',
                },
              }
            : {}
        }
      >
        {exporting ? 'Exportiere...' : buttonLabel}
      </Button>

      <Menu
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
      >
        {exportOptions.map(option => (
          <MenuItem key={option.format} onClick={() => handleExport(option)} disabled={exporting}>
            <ListItemIcon>
              {exportingFormat === option.format ? <CircularProgress size={20} /> : option.icon}
            </ListItemIcon>
            <ListItemText primary={option.label} secondary={option.extension} />
          </MenuItem>
        ))}
      </Menu>
    </>
  );
};
