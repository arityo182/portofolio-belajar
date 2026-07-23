import { Link } from "react-router-dom";
import { MessageCircle } from "lucide-react";
import { C } from "../../core/theme";

export default function ChatbotCTA() {
  return (
    <section className="mx-auto max-w-6xl px-5 pb-16">
      <div
        className="flex flex-col items-center gap-6 rounded-[2rem] p-10 text-center md:flex-row md:text-left"
        style={{ backgroundColor: C.blue }}
      >
        <div
          className="flex h-16 w-16 flex-shrink-0 items-center justify-center rounded-full"
          style={{ backgroundColor: C.white }}
        >
          <MessageCircle size={30} color={C.navy} />
        </div>
        <div className="flex-1">
          <h2 className="text-2xl font-bold" style={{ color: C.navyDark }}>
            Punya pertanyaan kesehatan?
          </h2>
          <p className="mt-1 text-sm" style={{ color: C.navyDark }}>
            Chatbot AI kami siap membantu seputar layanan &amp; info kesehatan, 24 jam.
          </p>
        </div>
        <Link
          to="/osteoporosis"
          className="rounded-full px-7 py-3 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5"
          style={{ backgroundColor: C.navy }}
        >
          Pelajari Lebih
        </Link>
      </div>
    </section>
  );
}
