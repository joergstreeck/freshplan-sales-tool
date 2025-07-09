import { SvgIconComponent } from '@mui/icons-material';

export interface NavigationSubItem {
  label: string;
  path: string;
  permissions?: string[];
}

export interface NavigationItemType {
  id: string;
  label: string;
  icon: SvgIconComponent;
  path: string;
  permissions?: string[];
  subItems?: NavigationSubItem[];
}

export interface NavigationConfig {
  items: NavigationItemType[];
}