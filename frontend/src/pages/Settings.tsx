import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import {Save, User as UserIcon, Mail, AlertCircle, CheckCircle2, Trash2} from 'lucide-react';
import {api} from "../api/axios.ts";

export default function Settings() {
    const { user, updateProfile, logout } = useAuth();

    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');

    const [isLoading, setIsLoading] = useState(false);
    const [successMsg, setSuccessMsg] = useState('');
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        if (user) {
            setUsername(user.username);
            setEmail(user.email || '');
        }
    }, [user]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setErrorMsg('');
        setSuccessMsg('');

        try {
            await updateProfile(username, email);
            setSuccessMsg('Profil byl úspěšně aktualizován!');

            setTimeout(() => setSuccessMsg(''), 3000);
        } catch (err: unknown) {
            const message = err instanceof Error ? err.message : 'Nastala neočekávaná chyba.';
            setErrorMsg(message);
        } finally {
            setIsLoading(false);
        }
    };

    const handleDeleteAccount = async () => {
        if (!window.confirm("Opravdu chcete nenávratně smazat svůj účet? Přijdete o všechny své sady, historii kvízů i postup ve studiu (flashcards).")) {
            return;
        }

        setIsLoading(true);
        setErrorMsg('');
        setSuccessMsg('');

        try {
            await api.delete('/users/me');

            if (typeof logout === 'function') {
                logout();
            } else {
                localStorage.removeItem('token');
                window.location.href = '/';
            }
        } catch (err: unknown) {
            const message = err instanceof Error ? err.message : 'Nepodařilo se smazat účet. Zkuste to prosím znovu.';
            setErrorMsg(message);
            setIsLoading(false);
        }
    };

    if (!user) return <p className="text-(--text)">Musíte být přihlášeni.</p>;

    return (
        <div className="max-w-2xl mx-auto space-y-6">
            <h1 className="text-3xl font-bold text-(--text-h)">Nastavení profilu</h1>

            <div className="bg-(--bg) border border-(--border) rounded-xl p-6 shadow-sm">
                <form onSubmit={handleSubmit} className="space-y-6">

                    {/* Username */}
                    <div>
                        <label className="block text-sm font-medium text-(--text) mb-2">Uživatelské jméno</label>
                        <div className="relative">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <UserIcon size={18} className="text-(--text) opacity-50" />
                            </div>
                            <input
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                className="pl-10 w-full bg-(--bg) border border-(--border) text-(--text) rounded-lg px-4 py-2 focus:outline-none focus:border-(--accent) transition-colors"
                                required
                                minLength={3}
                                maxLength={50}
                            />
                        </div>
                    </div>

                    {/* E-mail */}
                    <div>
                        <label className="block text-sm font-medium text-(--text) mb-2">E-mailová adresa</label>
                        <div className="relative">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <Mail size={18} className="text-(--text) opacity-50" />
                            </div>
                            <input
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                className="pl-10 w-full bg-(--bg) border border-(--border) text-(--text) rounded-lg px-4 py-2 focus:outline-none focus:border-(--accent) transition-colors"
                                required
                            />
                        </div>
                    </div>

                    {/* Succ/Err message */}
                    {errorMsg && (
                        <div className="flex items-center gap-2 text-red-500 bg-red-500/10 p-3 rounded-lg text-sm">
                            <AlertCircle size={18} /> {errorMsg}
                        </div>
                    )}
                    {successMsg && (
                        <div className="flex items-center gap-2 text-green-500 bg-green-500/10 p-3 rounded-lg text-sm">
                            <CheckCircle2 size={18} /> {successMsg}
                        </div>
                    )}

                    {/* Save */}
                    <div className="pt-4 flex justify-end">
                        <button
                            type="submit"
                            disabled={isLoading || (username === user.username && email === user.email)}
                            className="flex items-center gap-2 bg-(--accent) text-(--bg) px-6 py-2 rounded-lg font-bold hover:opacity-90 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            <Save size={18} />
                            {isLoading ? 'Ukládám...' : 'Uložit změny'}
                        </button>
                    </div>
                </form>
            </div>
            <div className="bg-red-500/5 border border-red-500/20 rounded-xl p-6 shadow-sm">
                <h2 className="text-xl font-bold text-red-500 mb-2">Smazání účtu</h2>
                <p className="text-(--text) text-sm mb-4">
                    Trvale přijdete o veškerou historii kvízů, vytvořené sady a postup ve studiu (flashcards). <strong>Tuto akci nelze vzít zpět.</strong>
                </p>
                <button
                    type="button"
                    onClick={handleDeleteAccount}
                    disabled={isLoading}
                    className="flex items-center gap-2 bg-red-500 text-white px-6 py-2 rounded-lg font-bold hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    <Trash2 size={18} />
                    {isLoading ? 'Zpracovávám...' : 'Smazat účet'}
                </button>
            </div>
        </div>
    );
}