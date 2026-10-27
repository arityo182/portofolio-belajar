/** Animasi 3 titik saat Senta sedang mengetik respons. */
export default function TypingIndicator() {
    return (<div className="flex items-start gap-3 px-4 py-3">
      {/* Avatar Senta */}
      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[#172E74] text-sm font-bold text-white">
        S
      </div>
      {/* Bouncing dots */}
      <div className="flex items-center gap-1 rounded-2xl rounded-tl-sm bg-gray-100 px-4 py-3">
        {[0, 1, 2].map((i) => (<span key={i} className="h-2 w-2 animate-bounce rounded-full bg-gray-400" style={{ animationDelay: `${i * 0.15}s` }}/>))}
      </div>
    </div>);
}
