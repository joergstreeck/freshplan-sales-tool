import React from 'react';
import { Box, Typography, Paper } from '@mui/material';
import { AdaptiveFormContainer } from '../adaptive/AdaptiveFormContainer';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import type { CustomerLocation } from '../../types/customer.types';
import type { LocationServiceData } from '../../stores/locationServicesStore';
import type { FieldDefinition } from '../../types/field.types';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';

interface ServiceFieldGroup {
  id: string;
  title: string;
  icon: string;
  fields: FieldDefinition[];
}

interface ServiceFieldsContainerProps {
  location: CustomerLocation;
  services: LocationServiceData;
  onChange: (field: string, value: unknown) => void;
  onBlur?: (field: string) => void;
  industry: string;
  errors?: Record<string, string>;
}

export const ServiceFieldsContainer: React.FC<ServiceFieldsContainerProps> = ({
  location,
  services,
  onChange,
  onBlur = () => {},
  industry,
  errors = {},
}) => {
  const { getFieldByKey } = useFieldDefinitions();

  // Get service groups based on industry
  const getServiceGroups = (): ServiceFieldGroup[] => {
    switch (industry) {
      case 'hotel':
        return [
          {
            id: 'breakfast',
            title: 'Frühstücksgeschäft',
            icon: '☕',
            fields: ['offersBreakfast', 'breakfastWarm', 'breakfastGuestsPerDay']
              .map(key => getFieldByKey(key))
              .filter(Boolean) as FieldDefinition[],
          },
          {
            id: 'meals',
            title: 'Mittag- und Abendessen',
            icon: '🍽️',
            fields: ['offersLunch', 'offersDinner']
              .map(key => getFieldByKey(key))
              .filter(Boolean) as FieldDefinition[],
          },
          {
            id: 'additional',
            title: 'Zusatzservices',
            icon: '🛎️',
            fields: ['offersRoomService', 'offersEvents', 'eventCapacity']
              .map(key => getFieldByKey(key))
              .filter(Boolean) as FieldDefinition[],
          },
          {
            id: 'capacity',
            title: 'Kapazität',
            icon: '🏨',
            fields: ['roomCount', 'averageOccupancy']
              .map(key => getFieldByKey(key))
              .filter(Boolean) as FieldDefinition[],
          },
        ];

      case 'krankenhaus':
        return [
          {
            id: 'patientMeals',
            title: 'Patientenverpflegung',
            icon: '🏥',
            fields: [
              {
                key: 'mealSystem',
                label: 'Verpflegungssystem',
                fieldType: 'select',
                options: [
                  { value: 'COOK_AND_SERVE', label: 'Cook & Serve' },
                  { value: 'COOK_AND_CHILL', label: 'Cook & Chill' },
                  { value: 'FROZEN', label: 'Tiefkühl' },
                ],
              },
              { key: 'bedsCount', label: 'Anzahl Betten', fieldType: 'number' },
              { key: 'mealsPerDay', label: 'Mahlzeiten/Tag', fieldType: 'number' },
            ] as FieldDefinition[],
          },
          {
            id: 'diets',
            title: 'Diätformen',
            icon: '🥗',
            fields: [
              {
                key: 'offersVegetarian',
                label: 'Vegetarisch',
                fieldType: 'select',
                options: [
                  { value: false, label: 'Nein' },
                  { value: true, label: 'Ja' },
                ],
              },
              {
                key: 'offersVegan',
                label: 'Vegan',
                fieldType: 'select',
                options: [
                  { value: false, label: 'Nein' },
                  { value: true, label: 'Ja' },
                ],
              },
              {
                key: 'offersHalal',
                label: 'Halal',
                fieldType: 'select',
                options: [
                  { value: false, label: 'Nein' },
                  { value: true, label: 'Ja' },
                ],
              },
              {
                key: 'offersKosher',
                label: 'Koscher',
                fieldType: 'select',
                options: [
                  { value: false, label: 'Nein' },
                  { value: true, label: 'Ja' },
                ],
              },
            ] as FieldDefinition[],
          },
        ];

      case 'betriebsrestaurant':
        return [
          {
            id: 'operation',
            title: 'Betriebszeiten',
            icon: '🏢',
            fields: [
              {
                key: 'operatingDays',
                label: 'Betriebstage/Woche',
                fieldType: 'number',
                min: 1,
                max: 7,
              },
              { key: 'lunchGuests', label: 'Mittagsgäste/Tag', fieldType: 'number' },
              {
                key: 'subsidized',
                label: 'Subventioniert',
                fieldType: 'select',
                options: [
                  { value: false, label: 'Nein' },
                  { value: true, label: 'Ja' },
                ],
              },
            ] as FieldDefinition[],
          },
        ];

      default:
        return [];
    }
  };

  const serviceGroups = getServiceGroups();

  if (serviceGroups.length === 0) {
    return (
      <Paper variant="outlined" sx={{ p: 3, textAlign: 'center' }}>
        <Typography color="text.secondary">
          Für die Branche "{industry}" sind noch keine Service-Felder definiert.
        </Typography>
      </Paper>
    );
  }

  return (
    <AdaptiveFormContainer>
      {serviceGroups.map(group => (
        <Box key={group.id} sx={{ mb: 4 }}>
          <Typography
            variant="h6"
            gutterBottom
            sx={{ display: 'flex', alignItems: 'center', gap: 1 }}
          >
            <span>{group.icon}</span>
            <span>{group.title}</span>
          </Typography>

          <Paper variant="outlined" sx={{ p: 2 }}>
            <DynamicFieldRenderer
              fields={group.fields}
              values={services}
              errors={errors}
              onChange={onChange}
              onBlur={onBlur}
            />
          </Paper>
        </Box>
      ))}
    </AdaptiveFormContainer>
  );
};
