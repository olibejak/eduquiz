import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import DueDecksWidget from "../components/DueDecksWidget.tsx";

interface QuizHistoryItem {
    id: number;
    date: string;
    rank: number;
}

export default function Home() {
    const { user } = useAuth();

    // Todo: remove placeholder
    const [quizHistory] = useState<QuizHistoryItem[]>([
        { id: 1, date: '2024-06-01', rank: 1 },
        { id: 2, date: '2024-06-02', rank: 3 },
        { id: 3, date: '2024-06-03', rank: 2 }
    ]);

    return (
        <div className="max-w-5xl">
            {/* Header */}
            <h1 className="text-3xl font-bold text-(--text-h) mb-8 transition-colors leading-tight">
                {user ? (
                    user.username
                ) : (
                    <span className="inline-flex flex-col">
                        <span>Anonymní uživatel</span>
                        <span className="mt-2 text-base font-normal text-(--text)">
                            Pro zobrazení historie kvízů a ukládání postupu flashcards se přihlašte.
                        </span>
                    </span>
                )}
            </h1>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">

                    {/* Table: quiz history */}
                    <div className="bg-(--bg) p-6 rounded-xl border border-(--border) shadow-sm transition-colors">
                        <h2 className="text-xl font-semibold mb-4 text-(--text-h)">Historie kvízů</h2>
                        <table className="w-full text-left border-collapse">
                            <thead>
                            <tr className="border-b-2 border-(--border)">
                                <th className="py-2 px-4 font-medium text-(--text)">Datum</th>
                                <th className="py-2 px-4 font-medium text-(--text)">Umístění</th>
                            </tr>
                            </thead>
                            <tbody>
                            {quizHistory.map((item) => (
                                <tr key={item.id} className="border-b border-(--border) last:border-0 hover:bg-(--accent-bg) transition-colors">
                                    <td className="py-3 px-4 text-(--text)">{item.date}</td>
                                    <td className="py-3 px-4 font-medium text-(--accent)">{item.rank}. místo</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>

                    {/* Table: flashcards history */}
                    <div className="md:col-span-1">
                        {user && (
                            <DueDecksWidget />
                        )}
                    </div>
                </div>

        </div>
    );
}