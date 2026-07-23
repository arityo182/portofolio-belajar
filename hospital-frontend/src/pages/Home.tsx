import Hero from "../components/home/Hero";
import TrustBar from "../components/home/TrustBar";
import Services from "../components/home/Services";
import OsteoHighlight from "../components/home/OsteoHighlight";
import Doctors from "../components/home/Doctors";
import ChatbotCTA from "../components/home/ChatbotCTA";

export default function Home() {
  return (
    <>
      <Hero />
      <TrustBar />
      <Services />
      <OsteoHighlight />
      <Doctors />
      <ChatbotCTA />
    </>
  );
}
