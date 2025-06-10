import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from './card';

describe('Card Components', () => {
  it('renders Card with correct classes', () => {
    render(<Card data-testid="card">Card Content</Card>);

    const card = screen.getByTestId('card');
    expect(card).toHaveClass('card');
  });

  it('renders CardHeader with correct classes', () => {
    render(
      <Card>
        <CardHeader data-testid="card-header">Header Content</CardHeader>
      </Card>
    );

    const header = screen.getByTestId('card-header');
    expect(header).toHaveClass('card-header');
  });

  it('renders CardTitle with correct classes', () => {
    render(
      <Card>
        <CardHeader>
          <CardTitle>Test Title</CardTitle>
        </CardHeader>
      </Card>
    );

    const title = screen.getByText('Test Title');
    expect(title).toHaveClass('card-title');
  });

  it('renders CardDescription with correct classes', () => {
    render(
      <Card>
        <CardHeader>
          <CardDescription>Test Description</CardDescription>
        </CardHeader>
      </Card>
    );

    const description = screen.getByText('Test Description');
    expect(description).toHaveClass('card-description');
  });

  it('renders CardContent with correct classes', () => {
    render(
      <Card>
        <CardContent data-testid="card-content">Content</CardContent>
      </Card>
    );

    const content = screen.getByTestId('card-content');
    expect(content).toHaveClass('card-content');
  });

  it('renders CardFooter with correct classes', () => {
    render(
      <Card>
        <CardFooter data-testid="card-footer">Footer Content</CardFooter>
      </Card>
    );

    const footer = screen.getByTestId('card-footer');
    expect(footer).toHaveClass('card-footer');
  });

  it('applies custom className to Card', () => {
    render(
      <Card className="custom-class" data-testid="card">
        Content
      </Card>
    );

    const card = screen.getByTestId('card');
    expect(card).toHaveClass('card', 'custom-class');
  });

  it('renders complete card structure', () => {
    render(
      <Card>
        <CardHeader>
          <CardTitle>Title</CardTitle>
          <CardDescription>Description</CardDescription>
        </CardHeader>
        <CardContent>
          <p>Main content</p>
        </CardContent>
        <CardFooter>
          <button>Action</button>
        </CardFooter>
      </Card>
    );

    expect(screen.getByText('Title')).toBeInTheDocument();
    expect(screen.getByText('Description')).toBeInTheDocument();
    expect(screen.getByText('Main content')).toBeInTheDocument();
    expect(screen.getByText('Action')).toBeInTheDocument();
  });
});
