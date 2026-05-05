/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, type ReactNode } from 'react';
import Cookies from 'js-cookie';
import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
    id: string;
    username: string;
    role: string;
    exp: number;
}

interface User {
    id: string;
    username: string;
    role: string;
}

interface AuthContextType {
    user: User | null;
    login: () => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Helper fun
// Info: keep out of component
const getUserFromToken = (token?: string): User | null => {
    if (!token) return null;
    try {
        const decoded = jwtDecode<JwtPayload>(token);
        if (decoded.exp * 1000 > Date.now()) {
            return {
                id: decoded.id,
                username: decoded.username,
                role: decoded.role,
            };
        } else {
            Cookies.remove('jwt_token');
        }
    } catch (error) {
        console.error("Neplatný token", error);
        Cookies.remove('jwt_token');
    }
    return null;
};

export function AuthProvider({ children }: { children: ReactNode }) {

    const [user, setUser] = useState<User | null>(() => {
        if (typeof window !== 'undefined') {
            const urlParams = new URLSearchParams(window.location.search);
            const tokenFromUrl = urlParams.get('token');

            if (tokenFromUrl) {
                Cookies.set('jwt_token', tokenFromUrl, { expires: 7 });
                window.history.replaceState({}, document.title, window.location.pathname);
                return getUserFromToken(tokenFromUrl);
            }
        }

        const existingToken = Cookies.get('jwt_token');
        return getUserFromToken(existingToken);
    });

    const login = () => {
        window.location.href = 'http://localhost:8080/oauth2/authorization/google';
    };

    const logout = () => {
        Cookies.remove('jwt_token');
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}