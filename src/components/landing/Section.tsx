import { motion } from "framer-motion"
import { Button } from "@/components/ui/button"
import Icon from "@/components/ui/icon"
import type { SectionProps } from "@/types"

export default function Section({ id, title, subtitle, content, isActive, showButton, buttonText, ip, ranks }: SectionProps) {
  return (
    <section
      id={id}
      className="relative h-screen w-full snap-start flex flex-col justify-center p-8 md:p-16 lg:p-24 overflow-hidden"
      style={id === 'hero' ? {
        backgroundImage: `linear-gradient(to bottom, rgba(0,0,0,0.55) 0%, rgba(0,0,0,0.75) 100%), url(https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/files/b264d2fa-6319-4ed6-bb7b-6658c8791f4e.jpg)`,
        backgroundSize: 'cover',
        backgroundPosition: 'center'
      } : {}}
    >
      {subtitle && (
        <motion.div
          className="mb-12"
          initial={{ opacity: 0, y: 20 }}
          animate={isActive ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.5 }}
        >
          {subtitle}
        </motion.div>
      )}
      <motion.h2
        className="text-4xl md:text-6xl lg:text-[5rem] xl:text-[6rem] font-bold leading-[1.1] tracking-tight max-w-4xl text-white"
        initial={{ opacity: 0, y: 50 }}
        animate={isActive ? { opacity: 1, y: 0 } : {}}
        transition={{ duration: 0.5 }}
      >
        {title}
      </motion.h2>
      {content && (
        <motion.p
          className="text-lg md:text-xl lg:text-2xl max-w-2xl mt-6 text-neutral-400"
          initial={{ opacity: 0, y: 50 }}
          animate={isActive ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.5, delay: 0.2 }}
        >
          {content}
        </motion.p>
      )}
      {ip && (
        <motion.div
          className="mt-10 flex items-center gap-4"
          initial={{ opacity: 0, y: 30 }}
          animate={isActive ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.5, delay: 0.3 }}
        >
          <div className="flex items-center gap-3 bg-white/5 border border-white/10 backdrop-blur rounded-xl px-6 py-4">
            <Icon name="Server" size={20} className="text-green-400" />
            <span className="text-neutral-400 text-sm">IP сервера:</span>
            <span className="text-white font-mono font-semibold text-lg tracking-wider">{ip}</span>
            <button
              onClick={() => navigator.clipboard.writeText(ip)}
              className="ml-2 text-neutral-500 hover:text-green-400 transition-colors"
              title="Скопировать"
            >
              <Icon name="Copy" size={16} />
            </button>
          </div>
        </motion.div>
      )}
      {ranks && ranks.length > 0 && (
        <motion.div
          className="mt-8 w-full overflow-x-auto pb-4"
          initial={{ opacity: 0, y: 40 }}
          animate={isActive ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.5, delay: 0.2 }}
        >
          <div className="flex gap-4 min-w-max">
            {ranks.map((rank, i) => (
              <motion.div
                key={rank.id}
                className="relative flex flex-col rounded-2xl overflow-hidden border bg-white/5 backdrop-blur w-44 flex-shrink-0 hover:scale-105 transition-transform cursor-pointer group"
                initial={{ opacity: 0, y: 30 }}
                animate={isActive ? { opacity: 1, y: 0 } : {}}
                transition={{ duration: 0.4, delay: 0.1 + i * 0.07 }}
                style={{ borderColor: rank.color + '55' }}
              >
                <div className="relative h-36 overflow-hidden">
                  <img
                    src={rank.image}
                    alt={rank.name}
                    className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent" />
                </div>
                <div className="p-3 flex flex-col gap-2 flex-1">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-white text-sm">{rank.name}</span>
                    <span className="font-bold text-sm" style={{ color: rank.color }}>{rank.price} ₽</span>
                  </div>
                  <ul className="flex flex-col gap-1">
                    {rank.perks.slice(0, 3).map((perk, j) => (
                      <li key={j} className="flex items-center gap-1.5 text-neutral-400 text-xs">
                        <Icon name="Check" size={10} className="flex-shrink-0" style={{ color: rank.color }} />
                        {perk}
                      </li>
                    ))}
                  </ul>
                  <Button
                    size="sm"
                    className="mt-auto w-full text-xs font-semibold"
                    style={{ backgroundColor: rank.color, color: '#000' }}
                  >
                    Купить
                  </Button>
                </div>
              </motion.div>
            ))}
          </div>
        </motion.div>
      )}

      {showButton && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={isActive ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.5, delay: 0.4 }}
          className="mt-12 md:mt-16"
        >
          <Button
            variant="outline"
            size="lg"
            className="text-green-400 bg-transparent border-green-400 hover:bg-green-400 hover:text-black transition-colors text-base font-semibold px-8"
          >
            {buttonText}
          </Button>
        </motion.div>
      )}
    </section>
  )
}