import { Link, Outlet } from 'react-router-dom';
import { Home, PlayCircle, Layers, Library, PlusSquare, Settings, HelpCircle, Moon, Sun, Menu, X, LogOut, User } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useEffect, useState } from "react";
import { GoogleLogin } from '@react-oauth/google';

export default function Layout() {
    const { user, login, register, logout, authError, clearError } = useAuth();
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

    const [isDark, setIsDark] = useState(() => {
        return localStorage.getItem('theme') === 'dark' ||
            (!localStorage.getItem('theme') && window.matchMedia('(prefers-color-scheme: dark)').matches);
    });

    useEffect(() => {
        if (isDark) {
            document.documentElement.classList.add('dark');
            localStorage.setItem('theme', 'dark');
        } else {
            document.documentElement.classList.remove('dark');
            localStorage.setItem('theme', 'light');
        }
    }, [isDark]);

    const toggleTheme = () => setIsDark(!isDark);
    const closeMobileMenu = () => setIsMobileMenuOpen(false);

    return (
        <div className="flex h-screen bg-(--bg) text-(--text) transition-colors relative overflow-hidden">

            {/* Dark background for mobiles */}
            {isMobileMenuOpen && (
                <div
                    className="fixed inset-0 z-20 bg-black/50 md:hidden transition-opacity"
                    onClick={closeMobileMenu}
                />
            )}

            {/* Sidebar */}
            <aside className={`
                fixed inset-y-0 left-0 z-30 w-64 bg-(--bg) border-r border-(--border) flex flex-col justify-between
                transform transition-transform duration-300 ease-in-out
                md:relative md:translate-x-0
                ${isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full'}
            `}>
                <div className="p-4 flex-1 overflow-y-auto">
                    {/* Logo + mobile button */}
                    <div className="flex items-center justify-between mb-8 px-4">
                        <div className="font-bold text-2xl text-(--accent) italic">EduQuiz</div>
                        <button
                            onClick={closeMobileMenu}
                            className="md:hidden p-1 hover:bg-(--accent-bg) rounded-lg text-(--text)"
                        >
                            <X size={24} />
                        </button>
                    </div>

                    <nav className="space-y-2">
                        <Link to="/" onClick={closeMobileMenu} className="flex items-center gap-3 px-4 py-2 hover:bg-(--accent-bg) rounded-lg transition-colors">
                            <Home size={20} /> Domů
                        </Link>
                        <Link to="/quiz" onClick={closeMobileMenu} className="flex items-center gap-3 px-4 py-2 hover:bg-(--accent-bg) rounded-lg transition-colors">
                            <PlayCircle size={20} /> Kvíz
                        </Link>
                        <Link to="/flashcards" onClick={closeMobileMenu} className="flex items-center gap-3 px-4 py-2 hover:bg-(--accent-bg) rounded-lg transition-colors">
                            <Layers size={20} /> Flashcards
                        </Link>
                        <Link to="/library" onClick={closeMobileMenu} className="flex items-center gap-3 px-4 py-2 hover:bg-(--accent-bg) rounded-lg transition-colors">
                            <Library size={20} /> Knihovna
                        </Link>
                        <Link to="/create" onClick={closeMobileMenu} className="flex items-center gap-3 px-4 py-2 hover:bg-(--accent-bg) rounded-lg transition-colors">
                            <PlusSquare size={20} /> Vytvořit
                        </Link>
                    </nav>
                </div>

                {/* Bottom part of sidebar */}
                <div className="p-4 border-t border-(--border) space-y-4">

                    {/* AUTH SECTION - ONLY VISIBLE ON MOBILE SIDEBAR */}
                    <div className="md:hidden space-y-3 pb-2">
                        {user ? (
                            <div className="space-y-1">
                                <div className="flex items-center gap-3 px-4 py-2 text-sm font-medium opacity-80">
                                    <User size={20} /> {user.username}
                                </div>
                                <button
                                    onClick={() => { logout(); closeMobileMenu(); }}
                                    className="flex items-center gap-3 px-4 py-2 w-full text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors text-left"
                                >
                                    <LogOut size={20} /> Odhlásit se
                                </button>
                            </div>
                        ) : (
                            <div className="flex flex-col gap-3 px-2">
                                <div className="w-full flex justify-center scale-95 origin-left">
                                    <GoogleLogin
                                        onSuccess={res => { login(res.credential!); closeMobileMenu(); }}
                                        onError={() => console.log('Login Failed')}
                                        text="signin_with"
                                        shape="rectangular"
                                    />
                                </div>
                                <div className="w-full flex justify-center scale-95 origin-left">
                                    <GoogleLogin
                                        onSuccess={res => { register(res.credential!); closeMobileMenu(); }}
                                        onError={() => console.log('Registration Failed')}
                                        text="signup_with"
                                        theme="filled_blue"
                                    />
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Settings and Help */}
                    <div className="space-y-1">
                        <Link to="/settings" onClick={closeMobileMenu} className="flex items-center gap-3 px-4 py-2 w-full hover:bg-(--accent-bg) rounded-lg transition-colors">
                            <Settings size={20} /> Nastavení
                        </Link>
                        <Link to={"/help"} onClick={closeMobileMenu} className="flex items-center gap-3 px-4 py-2 w-full hover:bg-(--accent-bg) rounded-lg transition-colors text-left">
                            <HelpCircle size={20} /> Nápověda
                        </Link>
                    </div>
                </div>
            </aside>

            {/* Topbar + Main */}
            <div className="flex-1 flex flex-col overflow-hidden w-full relative z-10">

                {/* Topbar */}
                <header className="h-16 bg-(--bg) border-b border-(--border) flex items-center justify-between md:justify-end px-4 md:px-6 transition-colors">

                    {/* Hamburger icon for mobiles */}
                    <div className="flex items-center gap-3">
                        <button
                            onClick={() => setIsMobileMenuOpen(true)}
                            className="md:hidden p-2 -ml-2 hover:bg-(--accent-bg) rounded-lg text-(--text)"
                        >
                            <Menu size={24} />
                        </button>
                        <span className="md:hidden font-bold text-xl text-(--accent) italic">EduQuiz</span>
                    </div>

                    <div className="flex items-center gap-4">
                        {/* AUTH SECTION - ONLY VISIBLE ON DESKTOP TOPBAR */}
                        <div className="hidden md:flex items-center gap-4">
                            {user ? (
                                <div className="flex items-center gap-4">
                                    <span className="text-sm font-medium">Uživatel: {user.username}</span>
                                    <button onClick={logout} className="text-sm text-red-500 hover:text-red-700 font-medium">Odhlásit se</button>
                                </div>
                            ) : (
                                <div className="flex gap-2 items-center">
                                    <div className="scale-90 origin-right">
                                        <GoogleLogin
                                            onSuccess={res => login(res.credential!)}
                                            onError={() => console.log('Login Failed')}
                                            text="signin_with"
                                            shape="rectangular"
                                        />
                                    </div>
                                    <div className="scale-90 origin-right">
                                        <GoogleLogin
                                            onSuccess={res => register(res.credential!)}
                                            onError={() => console.log('Registration Failed')}
                                            text="signup_with"
                                            theme="filled_blue"
                                        />
                                    </div>
                                </div>
                            )}
                        </div>

                        <button
                            onClick={toggleTheme}
                            className="flex items-center gap-2 border border-(--border) px-3 py-1.5 rounded-lg hover:bg-(--accent-bg) transition-colors"
                        >
                            {isDark ?
                                <Sun size={16} className="text-yellow-500"/>
                                :
                                <Moon size={16} className="text-purple-400"/>
                            }
                        </button>
                    </div>
                </header>

                {authError && (
                    <div className="bg-red-100 dark:bg-red-900/30 border-l-4 border-red-500 text-red-700 dark:text-red-400 p-4 m-4 md:m-6 mb-0 rounded shadow-sm flex justify-between items-center transition-colors">
                        <p className="font-medium text-sm md:text-base">{authError}</p>
                        <button onClick={clearError} className="text-red-500 hover:text-red-700 font-bold ml-4">✕</button>
                    </div>
                )}

                {/* Main */}
                <main className="flex-1 overflow-auto p-4 md:p-6">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}