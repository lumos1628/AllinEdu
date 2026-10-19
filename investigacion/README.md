# Investigación — organización y flujo

Toda la evidencia de campo del proyecto. La síntesis citable vive en
[`../EVIDENCIA.md`](../EVIDENCIA.md); aquí está la materia prima y los pasos intermedios.

## Estructura

```
investigacion/
├── README.md                 este archivo
├── transcribir.py            transcripción local con faster-whisper
├── rondas/                   VERSIONADO — una síntesis por ronda de campo
│   └── 2026-10-16-relampago.md
├── transcripciones/          VERSIONADO — transcripción anonimizada y verificada
│   ├── PLANTILLA.md
│   └── D##-<nivel>-<gestion>-<AAAA-MM-DD>.md
└── crudo/                    NO VERSIONADO — contiene voz y nombres reales
    └── <AAAA-MM-DD>-<nombre-de-ronda>/
        ├── audio/            grabaciones originales
        └── texto/            transcripción automática, sin verificar
```

**La organización es por ronda de campo, no por tema.** Cada carpeta de `crudo/` es una
fecha de salida a campo. Esto no es cosmético: los documentos del curso se entregan semana
a semana, y cada entrega solo puede citar lo que ya se sabía en ese momento. Mezclar rondas
produce documentos que afirman en la semana 2 cosas que se descubrieron en la semana 9.

## Rondas registradas

| Ronda | Fecha | Material | Alimenta |
|---|---|---|---|
| `2026-09-20-empatizar` | 20 set | 1 entrevista larga (45 min) + 3 audios (16 min) | Taller 02 — Empatizar |
| `2026-09-25-26-profundizacion` | 25–26 set | 5 audios (78 min) | Talleres 03 y 04 — Definir |
| `2026-10-16-relampago` | 16 oct | 17 audios (3 h 37 min) | Talleres 09–13 |

## Flujo de trabajo

1. **Volcar** las grabaciones en `crudo/<fecha>-<ronda>/audio/`.
2. **Transcribir** con el comando de abajo. El script recorre todas las rondas y salta lo
   ya transcrito; la salida va a `texto/` dentro de la misma ronda.
3. **Leer y sintetizar** en `rondas/<fecha>-<ronda>.md`: mapa de audios, hallazgos y límites.
4. **Anonimizar** en `transcripciones/` solo lo que se vaya a citar, con `PLANTILLA.md`.
5. **Promover** a `../EVIDENCIA.md` lo que resista verificación contra el audio.

### Transcribir

```bash
python3 -m venv /tmp/whisper-venv
/tmp/whisper-venv/bin/pip install faster-whisper nvidia-cublas-cu12 'nvidia-cudnn-cu12==9.*'
NVLIBS=$(find /tmp/whisper-venv/lib/python3.12/site-packages/nvidia -name lib -type d | tr '\n' ':')
LD_LIBRARY_PATH="$NVLIBS" /tmp/whisper-venv/bin/python investigacion/transcribir.py
```

faster-whisper `large-v3` sobre GPU, todo en local. Importa que sea local: los audios tienen
voz de docentes identificables y no se consintió enviarlos a un servicio externo.
`MODELO=small` para una pasada rápida.

Detalles que no son accidentales:

- `initial_prompt` carga vocabulario del dominio. Sin él, Whisper escribe "Hugel" por "UGEL".
- `condition_on_previous_text=False` evita los bucles de repetición en audio largo.
- Los timestamps `[mm:ss]` existen para cumplir la regla de `EVIDENCIA.md`: una cita textual
  solo se usa si fue verificada contra el audio.

### Lo que este pipeline no hace

No separa hablantes. Si un audio tiene entrevistador y docente mezclados, los turnos se marcan
a mano al pasar a `transcripciones/`.

## Reglas no negociables

- `crudo/` nunca se versiona. Contiene voz y, en algunos audios, nombres y teléfonos.
- Sin nombres propios en lo versionado. Solo `D<n>` y el perfil.
- Toda cifra viaja con su fuente y su `n`.
- Lo que el equipo infiere se marca como inferencia, no como hallazgo.
- La transcripción automática **no es evidencia**: es borrador sin verificar.
