#!/usr/bin/env python3
"""Transcribe entrevistas docentes (es-PE) con faster-whisper. Todo local."""
import sys, os, time, pathlib, subprocess
import numpy as np

CRUDO = pathlib.Path("/home/gabriel/Escritorio/Repos/AllinEdu/investigacion/crudo")
EXTS = {".mp3", ".mp4", ".m4a", ".ogg", ".opus", ".wav"}


def pendientes():
    """Recorre crudo/<ronda>/audio/ y devuelve (audio, destino) por transcribir."""
    tareas = []
    for ronda in sorted(d for d in CRUDO.iterdir() if d.is_dir()):
        aud, txt = ronda / "audio", ronda / "texto"
        if not aud.is_dir():
            continue
        txt.mkdir(exist_ok=True)
        for a in sorted(aud.iterdir()):
            if a.suffix.lower() in EXTS:
                tareas.append((a, txt / f"{a.stem}.md"))
    return tareas

# Vocabulario del dominio: sin esto, Whisper escribe "Hugel", "Sagie", "Minedu" mal.
PROMPT = (
    "Entrevista a docente de educación básica en Perú. Vocabulario: sesión de aprendizaje, "
    "ficha de trabajo, unidad didáctica, propósito de aprendizaje, criterios de evaluación, "
    "competencia, capacidad, desempeño, currículo nacional, registro auxiliar, UGEL, Minedu, "
    "SIAGIE, apoderado, aula, primaria, secundaria, trabajo colegiado, conclusiones descriptivas, "
    "retroalimentación, rúbrica, EBR, director, coordinador pedagógico."
)

def decodificar(ruta):
    """Decodifica a 16 kHz mono float32 con ffmpeg.

    faster-whisper decodifica con PyAV, pero av>=19 quitó el argumento
    `metadata_errors` que faster-whisper 1.2.1 todavía pasa. ffmpeg evita
    esa incompatibilidad y maneja m4a de grabadora de celular sin problemas.
    """
    cmd = ["ffmpeg", "-nostdin", "-threads", "0", "-i", str(ruta),
           "-f", "f32le", "-ac", "1", "-acodec", "pcm_f32le", "-ar", "16000", "-"]
    r = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True)
    return np.frombuffer(r.stdout, dtype=np.float32).copy()


def main():
    from faster_whisper import WhisperModel
    modelo = os.environ.get("MODELO", "large-v3")
    try:
        m = WhisperModel(modelo, device="cuda", compute_type="float16")
        disp = "cuda/float16"
    except Exception as e:
        print(f"[!] GPU no disponible ({type(e).__name__}: {e}). Caigo a CPU.", flush=True)
        m = WhisperModel(modelo, device="cpu", compute_type="int8", cpu_threads=12)
        disp = "cpu/int8"
    print(f"[i] modelo={modelo} en {disp}\n", flush=True)

    tareas = pendientes()
    if not tareas:
        print("[!] No hay audios en crudo/<ronda>/audio/"); return 1

    for p, destino in tareas:
        if destino.exists() and destino.stat().st_size > 0:
            print(f"[=] {p.stem}: ya transcrito, salto.", flush=True)
            continue
        t0 = time.time()
        try:
            audio = decodificar(p)
        except subprocess.CalledProcessError as e:
            print(f"[x] {p.stem}: ffmpeg falló. {e.stderr.decode()[-300:]}", flush=True)
            continue
        dur_min = len(audio) / 16000 / 60
        print(f"[>] {p.stem}: {dur_min:.1f} min, transcribiendo...", flush=True)
        segmentos, info = m.transcribe(
            audio,
            language="es",
            beam_size=5,
            vad_filter=True,
            vad_parameters=dict(min_silence_duration_ms=700),
            condition_on_previous_text=False,  # evita bucles de repetición en audio largo
            initial_prompt=PROMPT,
        )
        lineas, n = [], 0
        with destino.open("w", encoding="utf-8") as fh:
            fh.write(f"# Transcripción automática — {p.name}\n\n")
            fh.write(f"> Generada con faster-whisper `{modelo}` en local. **Sin verificar.**\n")
            fh.write(f"> Duración del audio: {info.duration/60:.1f} min. "
                     f"Idioma detectado: {info.language} ({info.language_probability:.0%}).\n")
            fh.write("> Los timestamps permiten verificar cada cita contra el audio antes de usarla.\n\n---\n\n")
            for s in segmentos:
                mm, ss = divmod(int(s.start), 60)
                linea = f"**[{mm:02d}:{ss:02d}]** {s.text.strip()}"
                fh.write(linea + "\n\n")
                fh.flush()
                n += 1
                if n % 25 == 0:
                    print(f"    ... {n} segmentos ({mm:02d}:{ss:02d})", flush=True)
        print(f"[ok] {p.stem}: {n} segmentos, {time.time()-t0:.0f}s -> {destino.name}", flush=True)
    print("\n[fin] Transcripciones en crudo/<ronda>/texto/", flush=True)
    return 0

if __name__ == "__main__":
    sys.exit(main())
