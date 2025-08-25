import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import Layout from './pages/Layout';
import Login from './pages/Login';
import Signup from './pages/Signup';
import App from './pages/App';

createRoot(document.getElementById('root')!).render(
		<AuthProvider>
			<BrowserRouter>
				<Routes>
					<Route path="/" element={<Layout />}> 
						<Route index element={<App />} />
						<Route path="login" element={<Login />} />
						<Route path="signup" element={<Signup />} />
					</Route>
				</Routes>
			</BrowserRouter>
		</AuthProvider>
);
