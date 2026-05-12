import { Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Lock } from 'lucide-react';

export default function ProtectedRoute() {
    const { user } = useAuth();
    const navigate = useNavigate();

    if (!user) {
        return (
            <div className="flex flex-col items-center justify-center min-h-[70vh] text-center px-4 animate-in fade-in duration-300">
                <div className="p-6 rounded-full mb-6" style={{ backgroundColor: 'var(--accent-bg)' }}>
                    <Lock size={56} style={{ color: 'var(--accent)' }} />
                </div>

                <h1 className="text-3xl md:text-4xl font-black mb-4" style={{ color: 'var(--text-h)' }}>
                    Přístup odepřen
                </h1>

                <p className="text-lg md:text-xl mb-8 max-w-md" style={{ color: 'var(--text)' }}>
                    Pro zobrazení této stránky se musíte nejprve přihlásit ke svému účtu.
                </p>

                <button
                    onClick={() => navigate('/')}
                    className="px-8 py-4 rounded-xl font-bold text-lg transition-transform active:scale-95 hover:-translate-y-1 shadow-md"
                    style={{ backgroundColor: 'var(--accent)', color: 'var(--bg)' }}
                >
                    Zpět na hlavní stránku
                </button>
            </div>
        );
    }

    return <Outlet />;
}