/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, type ReactNode } from 'react';
import axios from 'axios';
import { api } from '../api/axios';

interface User {
    id: string;
    username: string;
    email: string;
    role: string;
}

interface AuthContextType {
    user: User | null;
    login: (googleToken: string) => Promise<void>;
    register: (googleToken: string) => Promise<void>;
    logout: () => void;
    updateProfile: (username: string, email: string) => Promise<void>;
    authError: string | null;
    clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
    const [authError, setAuthError] = useState<string | null>(null);

    const [user, setUser] = useState<User | null>(() => {
        const saved = localStorage.getItem('user_info');
        return saved ? JSON.parse(saved) : null;
    });

    const authenticate = async (googleToken: string, endpoint: string) => {
        try {
            setAuthError(null);

            const response = await api.post(endpoint, {googleToken: googleToken });

            const userData = response.data;
            setUser(userData);
            localStorage.setItem('user_info', JSON.stringify(userData));

        } catch (error: unknown) {
            if (axios.isAxiosError(error)) {
                setAuthError(error.response?.data?.message || "Chyba autentizace");
            }
        }
    };

    const updateProfile = async (username: string, email: string) => {
        try {
            const response = await api.patch('/users/me', { username, email });
            const updatedUser = response.data;
            setUser(updatedUser);
            localStorage.setItem('user_info', JSON.stringify(updatedUser));
        } catch (error: unknown) {
            if (axios.isAxiosError(error)) {
                throw new Error(error.response?.data?.message || "Nepodařilo se aktualizovat profil.", { cause: error });
            }
            throw error;
        }
    };

    const login = (token: string) => authenticate(token, '/auth/login/google');
    const register = (token: string) => authenticate(token, '/auth/register/google');

    const logout = async () => {
        try {
            await api.post('/auth/logout');
        } catch {
            console.warn("Logout error, forcing local state clear.");
        } finally {
            localStorage.removeItem('user_info');
            setUser(null);
        }
    };

    return (
        <AuthContext.Provider value={{ user, login, register, logout, updateProfile, authError, clearError: () => setAuthError(null) }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within AuthProvider');
    return context;

};