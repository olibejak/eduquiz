import { useState } from 'react';
import {
    HelpCircle,
    ChevronDown,
    ChevronUp,
    BookOpen,
    PlayCircle,
    Layers,
    CheckCircle2,
    Zap,
    PenTool
} from 'lucide-react';
import {Link} from "react-router-dom";

export default function Help() {
    const [openIndex, setOpenIndex] = useState<number | null>(0);

    const helpSections = [
        {
            title: 'Kvízy v reálném čase',
            icon: <PlayCircle size={24} />,
            color: 'text-blue-500',
            content: (
                <div className="space-y-3">
                    <p>
                        Interaktivní režim ideální pro třídy nebo skupiny. Hostitel (např. učitel) založí místnost a nasdílí ostatním <strong>PIN kód</strong>.
                        Jakmile hru odstartuje, všichni hráči vidí otázky současně na svých zařízeních.
                    </p>
                    <ul className="space-y-2 text-sm">
                        <li className="flex items-center gap-2"><CheckCircle2 size={16} className="text-[var(--accent)]" /> Kvíz může hrát i založit kdokoli bez nutnosti registrace.</li>
                        <li className="flex items-center gap-2"><CheckCircle2 size={16} className="text-[var(--accent)]" /> Hostitel může přidat více sad otázek najednou.</li>
                        <li className="flex items-center gap-2"><CheckCircle2 size={16} className="text-[var(--accent)]" /> Hostilel může zvolit, zdali bude hrát také.</li>
                    </ul>
                </div>
            )
        },
        {
            title: 'Flashcards a chytré opakování',
            icon: <Layers size={24} />,
            color: 'text-green-500',
            content: (
                <div className="space-y-3">
                    <p>
                        Pro samostudium aplikace využívá metodu <strong>rozloženého opakování (Spaced Repetition)</strong>.
                        Kartičky, které vám dělají problém, aplikace nabídne častěji, zatímco ty známé odloží na později.
                    </p>
                    <div className="bg-[var(--accent-bg)] p-4 rounded-xl text-sm">
                        <strong className="text-[var(--text-h)] block mb-1">Hosté vs. Přihlášení uživatelé:</strong>
                        Jako host můžete studovat v nekonečné smyčce "nanečisto". Pro ukládání postupu a výpočet intervalů je nutné přihlášení.
                    </div>
                </div>
            )
        },
        {
            title: 'Knihovna a viditelnost sad',
            icon: <BookOpen size={24} />,
            color: 'text-purple-500',
            content: (
                <div className="space-y-3">
                    <p>
                        V sekci Knihovna najdete veřejné sady od ostatních uživatelů. Oblíbené sady si můžete uložit kliknutím na srdíčko.
                    </p>
                    <p>
                        U vlastních sad volíte mezi <strong>Veřejnou</strong>, kterou uvidí všichni, a <strong>Soukromou</strong>,
                        kterou vidíte jen vy (stále ji můžete spustit ve kvízu).
                    </p>
                </div>
            )
        },
        {
            title: 'Typy otázek při tvorbě',
            icon: <PenTool size={24} />,
            color: 'text-orange-500',
            content: (
                <ul className="space-y-4">
                    <li className="flex gap-3">
                        <Zap size={18} className="text-[var(--accent)] flex-shrink-0 mt-1" />
                        <span><strong>Výběr správné odpovědi:</strong> Klasické ABCD otázky s jednou či více správnými odpověďmi.</span>
                    </li>
                    <li className="flex gap-3">
                        <Zap size={18} className="text-[var(--accent)] flex-shrink-0 mt-1" />
                        <span><strong>Psací:</strong> Vyžadují vepsání přesného textu (možné nastavic více správných možností).</span>
                    </li>
                    <li className="flex gap-3">
                        <Zap size={18} className="text-[var(--accent)] flex-shrink-0 mt-1" />
                        <span><strong>Číselné:</strong> Vepsání správného čísla.</span>
                    </li>
                    <li className="flex gap-3">
                        <Zap size={18} className="text-[var(--accent)] flex-shrink-0 mt-1" />
                        <span><strong>Spojovací:</strong> Párování pojmů a definic do dvojic.</span>
                    </li>
                </ul>
            )
        }
    ];

    return (
        <div className="max-w-4xl mx-auto p-4 md:p-6 space-y-8 animate-in fade-in duration-300">
            {/* Header */}
            <div className="text-center space-y-4 pt-4 md:pt-8 mb-10">
                <div className="inline-flex items-center justify-center p-4 rounded-full mb-2 bg-[var(--accent-bg)] text-[var(--accent)]">
                    <HelpCircle size={48} />
                </div>
                <h1 className="text-3xl md:text-5xl font-black text-[var(--text-h)]">
                    Nápověda
                </h1>
                <p className="text-lg md:text-xl text-[var(--text)] max-w-2xl mx-auto">
                    Zde najdete popis funkčnosti EduQuizu.
                </p>
            </div>

            {/* Main functions */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-12">
                <div className="p-6 rounded-2xl border border-[var(--border)] bg-[var(--bg)] shadow-sm text-center space-y-3">
                    <PlayCircle size={32} className="mx-auto text-[var(--accent)]" />
                    <h3 className="font-bold text-lg text-[var(--text-h)]">Kvíz</h3>
                    <p className="text-sm text-[var(--text)]">Soutěžte s ostatními v reálném čase.</p>
                </div>
                <div className="p-6 rounded-2xl border border-[var(--border)] bg-[var(--bg)] shadow-sm text-center space-y-3">
                    <Layers size={32} className="mx-auto text-[var(--accent)]" />
                    <h3 className="font-bold text-lg text-[var(--text-h)]">Flashcards</h3>
                    <p className="text-sm text-[var(--text)]">Učte se efektivně pomocí spaced repetition.</p>
                </div>
                <div className="p-6 rounded-2xl border border-[var(--border)] bg-[var(--bg)] shadow-sm text-center space-y-3">
                    <BookOpen size={32} className="mx-auto text-[var(--accent)]" />
                    <h3 className="font-bold text-lg text-[var(--text-h)]">Knihovna</h3>
                    <p className="text-sm text-[var(--text)]">Objevujte veřejné sady ostatních uživatelů.</p>
                </div>
            </div>

            {/* Akordeon sekce */}
            <div className="space-y-3">
                {helpSections.map((section, idx) => {
                    const isOpen = openIndex === idx;
                    return (
                        <div
                            key={idx}
                            className="border border-[var(--border)] rounded-2xl overflow-hidden bg-[var(--bg)] shadow-sm transition-all"
                        >
                            <button
                                onClick={() => setOpenIndex(isOpen ? null : idx)}
                                className="w-full flex items-center justify-between p-5 text-left hover:bg-[var(--accent-bg)] transition-colors"
                            >
                                <div className="flex items-center gap-4">
                                    <div className={`${section.color} opacity-80`}>{section.icon}</div>
                                    <span className="font-bold text-[var(--text-h)] text-lg">{section.title}</span>
                                </div>
                                {isOpen ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
                            </button>

                            {isOpen && (
                                <div className="p-5 pt-0 text-[var(--text)] border-t border-[var(--border)] animate-in slide-in-from-top-2 duration-200">
                                    <div className="pt-4 leading-relaxed">
                                        {section.content}
                                    </div>
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>

            {/* Rozšířená sekce s odkazy na konci */}
            <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mt-12 pt-6 border-t border-[var(--border)]">
                <Link
                    to="/quiz"
                    className="w-full sm:w-auto px-8 py-4 font-bold rounded-xl transition-all active:scale-95 shadow-md text-center"
                    style={{ backgroundColor: 'var(--accent)', color: 'var(--bg)' }}
                >
                    Zadat PIN a hrát
                </Link>
                <Link
                    to="/flashcards"
                    className="w-full sm:w-auto px-8 py-4 font-bold rounded-xl border-2 transition-all active:scale-95 hover:bg-[var(--accent-bg)] text-center"
                    style={{ borderColor: 'var(--border)', color: 'var(--text-h)' }}
                >
                    Studovat kartičky
                </Link>
            </div>
        </div>
    );
}