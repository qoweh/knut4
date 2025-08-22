import { describe, it, expect, beforeEach } from 'vitest';
import { useAuthStore } from './authStore';

describe('AuthStore', () => {
  beforeEach(() => {
    // Reset store state before each test
    useAuthStore.getState().logout();
  });

  it('should have initial state', () => {
    const { token, user, isAuthenticated } = useAuthStore.getState();
    expect(token).toBeNull();
    expect(user).toBeNull();
    expect(isAuthenticated).toBe(false);
  });

  it('should set authentication state', () => {
    const mockUser = {
      id: '1',
      email: 'test@example.com',
      name: 'Test User',
    };
    const mockToken = 'test-token';

    useAuthStore.getState().setAuth(mockToken, mockUser);

    const { token, user, isAuthenticated } = useAuthStore.getState();
    expect(token).toBe(mockToken);
    expect(user).toEqual(mockUser);
    expect(isAuthenticated).toBe(true);
  });

  it('should logout user', () => {
    const mockUser = {
      id: '1',
      email: 'test@example.com',
      name: 'Test User',
    };
    const mockToken = 'test-token';

    // First set auth
    useAuthStore.getState().setAuth(mockToken, mockUser);
    expect(useAuthStore.getState().isAuthenticated).toBe(true);

    // Then logout
    useAuthStore.getState().logout();

    const { token, user, isAuthenticated } = useAuthStore.getState();
    expect(token).toBeNull();
    expect(user).toBeNull();
    expect(isAuthenticated).toBe(false);
  });
});