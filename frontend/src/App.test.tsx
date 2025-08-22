import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import App from './App';

describe('App', () => {
  it('renders without crashing', () => {
    render(<App />);
    expect(screen.getByText('Knut4')).toBeInTheDocument();
  });

  it('displays home page content', () => {
    render(<App />);
    expect(screen.getByText('Welcome to Knut4')).toBeInTheDocument();
    expect(
      screen.getByText('Your personalized recommendation platform')
    ).toBeInTheDocument();
  });

  it('displays navigation links', () => {
    render(<App />);
    expect(screen.getByText('Home')).toBeInTheDocument();
    expect(screen.getByText('Login')).toBeInTheDocument();
    expect(screen.getByText('Sign Up')).toBeInTheDocument();
  });
});
