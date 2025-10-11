import { setupServer } from 'msw/node';
import { handlers } from './handlers';

// This configures a request interception server with the given request handlers for Node.js tests.
export const server = setupServer(...handlers);
