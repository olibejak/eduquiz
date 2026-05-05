import { Link, Outlet } from 'react-router-dom';
import { Home, PlayCircle, Layers, Library, PlusSquare, Settings, HelpCircle, Moon } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Layout() {
    const { user, login, logout } = useAuth();
    return (
        <div className="flex h-screen bg-gray-50 text-gray-900">

            {/* Sidebar */}
            <aside className="w-64 bg-white border-r border-gray-200 flex flex-col justify-between">
                <div className="p-4">
                    <div className="font-bold text-2xl text-blue-600 mb-8 px-4">Tabone</div>
                    <nav className="space-y-2">
                        <Link to="/" className="flex items-center gap-3 px-4 py-2 hover:bg-gray-100 rounded-lg">
                            <Home size={20} /> Domů
                        </Link>
                        <Link to="/quiz" className="flex items-center gap-3 px-4 py-2 hover:bg-gray-100 rounded-lg">
                            <PlayCircle size={20} /> Kvíz
                        </Link>
                        <Link to="/flashcards" className="flex items-center gap-3 px-4 py-2 hover:bg-gray-100 rounded-lg">
                            <Layers size={20} /> Flashcards
                        </Link>
                        <Link to="/library" className="flex items-center gap-3 px-4 py-2 hover:bg-gray-100 rounded-lg">
                            <Library size={20} /> Knihovna
                        </Link>
                        <Link to="/create" className="flex items-center gap-3 px-4 py-2 hover:bg-gray-100 rounded-lg">
                            <PlusSquare size={20} /> Vytvořit
                        </Link>
                    </nav>
                </div>

                <div className="p-4 space-y-2 border-t border-gray-100">
                    <button className="flex items-center gap-3 px-4 py-2 w-full hover:bg-gray-100 rounded-lg">
                        <Settings size={20} /> Nastavení
                    </button>
                    <button className="flex items-center gap-3 px-4 py-2 w-full hover:bg-gray-100 rounded-lg">
                        <HelpCircle size={20} /> Nápověda
                    </button>
                </div>
            </aside>

            {/* Topbar + Main ) */}
            <div className="flex-1 flex flex-col overflow-hidden">

                {/* Topbar */}
                <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-end px-6 gap-4">
                    {user ? (
                        <div className="flex items-center gap-4">
                            <span className="text-sm font-medium text-gray-700">Ahoj, {user.name}</span>
                            <button onClick={logout} className="text-sm text-red-600 hover:text-red-800 font-medium">Odhlásit se</button>
                        </div>
                    ) : (
                        <button onClick={login} className="text-sm font-medium text-blue-600 hover:text-blue-800">
                            Registrovat se / Přihlásit se
                        </button>
                    )}

                    <button className="flex items-center gap-2 border border-gray-300 px-3 py-1.5 rounded-lg hover:bg-gray-50">
                        <Moon size={16} /> Noční režim
                    </button>
                </header>

                {/* Main */}
                <main className="flex-1 overflow-auto p-6">
                    {/* Pages */}
                    <Outlet />
                </main>

            </div>
        </div>
    );
}