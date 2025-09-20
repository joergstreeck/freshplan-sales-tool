import React from 'react';
import { RouteObject } from 'react-router-dom';
import HelpHubPage from '../components/help/HelpHubPage';
import FollowUpWizard from '../components/guided/FollowUpWizard';
import ROIMiniCheck from '../components/guided/ROIMiniCheck';

const helpRoutes: RouteObject = {
  path: '/hilfe',
  children: [
    { index: true, element: <HelpHubPage/> },
    { path: 'follow-up', element: <FollowUpWizard/> },
    { path: 'roi-check', element: <ROIMiniCheck/> },
  ]
};

export default helpRoutes;
