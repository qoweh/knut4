# Knut4 Frontend

A modern React frontend built with Vite, TypeScript, and React Router.

## Features

- **Framework**: Vite + React 19 + TypeScript
- **Routing**: React Router with routes for:
  - `/` - Home page
  - `/auth/login` - Login page
  - `/auth/signup` - Signup page
  - `/recommendations` - Recommendations page
  - `/me` - User profile page
- **State Management**: Zustand for auth state with persistence
- **HTTP Client**: Axios with automatic token injection and response interceptors
- **Styling**: CSS Modules for component-scoped styling
- **Testing**: Vitest + React Testing Library
- **Code Quality**: ESLint + Prettier

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm

### Installation

```bash
cd frontend
npm install
```

### Development

```bash
# Start development server
npm run dev

# Run tests
npm run test

# Run tests in watch mode
npm run test:ui

# Build for production
npm run build

# Preview production build
npm run preview
```

### Code Quality

```bash
# Run ESLint
npm run lint

# Fix ESLint issues
npm run lint:fix

# Format code with Prettier
npm run format
```

## Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── Navigation.tsx
│   └── Navigation.module.css
├── pages/              # Page components
│   ├── Home.tsx
│   ├── Profile.tsx
│   ├── Recommendations.tsx
│   └── auth/
│       ├── Login.tsx
│       └── Signup.tsx
├── stores/             # Zustand stores
│   └── authStore.ts
├── lib/                # Utilities and configurations
│   └── axios.ts
├── App.tsx             # Main app component
└── main.tsx           # App entry point
```

## State Management

The app uses Zustand for state management with persistence:

- **Auth Store**: Manages user authentication state (token, user info)
- **Persistence**: Auth state is automatically persisted to localStorage

## API Configuration

Axios is configured with:
- Base URL from environment variable `VITE_API_BASE_URL`
- Automatic Bearer token injection from auth store
- Response interceptor for handling 401 errors

## Environment Variables

Create a `.env` file in the frontend directory:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## Testing

The project uses Vitest and React Testing Library for testing:

- Component tests for UI behavior
- Store tests for state management
- Integration tests for routing

Run tests with `npm run test` or `npm run test:ui` for the interactive UI.
