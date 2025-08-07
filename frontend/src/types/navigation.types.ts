import React from 'react';

export interface NavigationSubItem {
  label: string;
  path: string;
  permissions?: string[];
}

export interface NavigationItemType {
  id: string;
  label: string;
  icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
  path: string;
  permissions?: string[];
  subItems?: NavigationSubItem[];
}

export interface NavigationConfig {
  items: NavigationItemType[];
}
