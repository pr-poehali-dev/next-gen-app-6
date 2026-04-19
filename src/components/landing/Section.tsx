import { useState } from "react"
import { motion } from "framer-motion"
import { Button } from "@/components/ui/button"
import Icon from "@/components/ui/icon"
import type { SectionProps, DonateRank, SocialLink } from "@/types"
import DonateModal from "./DonateModal"

export default function Section({ id, title, subtitle, content, isActive, showButton, buttonText, ip, ranks, socials }: SectionProps) {
  const [selectedRank, setSelectedRank] = useState<DonateRank | null>(null)

  return (
    <>
      <section
        id={id}
        className="relative h-screen w-full snap-start flex flex-col justify-center p-8 md:p-16 lg:p-24 overflow-hidden"
        style={id === 'hero' ? {
          backgroundImage: `linear-gradient(to bottom, rgba(0,0,0,0.55) 0%, rgba(0,0,0,0.75) 100%), url(https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/e08139b8-602c-4afd-bcd8-735beae543ea.png)`,
          backgroundSize: 'cover',
          backgroundPosition: 'center'
        } : {}}
      >
        {subtitle && (
          <motion.div
            className="mb-8"
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
            <div className="flex gap-4 min-w-max pb-2">
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
                    <span className="font-bold text-xs px-2 py-0.5 rounded-md w-fit" style={{ color: rank.color, backgroundColor: rank.color + '22', border: `1px solid ${rank.color}55` }}>
                      [{rank.name}]
                    </span>
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-white text-sm">{rank.name}</span>
                      <span className="font-bold text-sm" style={{ color: rank.color }}>{rank.price} ₽</span>
                    </div>
                    <ul className="flex flex-col gap-1">
                      {rank.perks.slice(1).map((perk, j) => (
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
                      onClick={() => setSelectedRank(rank)}
                    >
                      Купить
                    </Button>
                  </div>
                </motion.div>
              ))}
            </div>
            <p className="text-neutral-500 text-xs mt-3 flex items-center gap-1.5">
              <Icon name="Clock" size={12} />
              Ранги выдаются вручную администратором в течение 2 дней
            </p>
          </motion.div>
        )}

        {socials && socials.length > 0 && (
          <motion.div
            className="mt-10 flex flex-col gap-6"
            initial={{ opacity: 0, y: 40 }}
            animate={isActive ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.5, delay: 0.3 }}
          >
            <div className="flex flex-wrap gap-5">
              {socials.map((social: SocialLink, i: number) => (
                <motion.a
                  key={social.label}
                  href={social.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-4 bg-white/5 border border-white/10 backdrop-blur rounded-2xl px-6 py-5 hover:bg-white/10 transition-all group"
                  initial={{ opacity: 0, y: 20 }}
                  animate={isActive ? { opacity: 1, y: 0 } : {}}
                  transition={{ duration: 0.4, delay: 0.3 + i * 0.1 }}
                >
                  <div
                    className="w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0"
                    style={{ backgroundColor: social.color + '22', border: `2px solid ${social.color}55` }}
                  >
                    <Icon name={social.icon} size={22} style={{ color: social.color }} />
                  </div>
                  <div>
                    <p className="text-white font-semibold text-base group-hover:underline">{social.label}</p>
                    <p className="text-neutral-500 text-xs">{social.url.replace('https://', '')}</p>
                  </div>
                  <Icon name="ArrowUpRight" size={16} className="text-neutral-600 group-hover:text-white transition-colors ml-2" />
                </motion.a>
              ))}
            </div>
            <motion.a
              href="https://t.me/She_bls"
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-3 text-neutral-500 hover:text-[#29B6F6] transition-colors text-sm mt-2 w-fit"
              initial={{ opacity: 0 }}
              animate={isActive ? { opacity: 1 } : {}}
              transition={{ duration: 0.4, delay: 0.6 }}
            >
              <Icon name="Clock" size={14} className="flex-shrink-0" />
              Если ранг не выдан более 2 дней — пиши в личку Telegram:
              <span className="text-[#29B6F6] font-medium">@She_bls</span>
            </motion.a>
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

      <DonateModal rank={selectedRank} onClose={() => setSelectedRank(null)} />
    </>
  )
}