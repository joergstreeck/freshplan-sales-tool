import { Box, Typography, Button, Chip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { UniversalExportButton } from '../../../components/export';

interface CustomerListHeaderProps {
  totalCount: number;
  onAddCustomer?: () => void;
}

export function CustomerListHeader({ totalCount, onAddCustomer }: CustomerListHeaderProps) {
  const navigate = useNavigate();

  // Default to navigation if no onAddCustomer provided
  const handleAddCustomer = React.useMemo(() => onAddCustomer || (() => console.log("Add customer")), [onAddCustomer]);