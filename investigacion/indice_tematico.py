#!/usr/bin/env python3
"""Índice temático sobre las transcripciones: cuántos audios tocan cada tema y dónde.

Sustituye a NotebookLM para este corpus: trabaja en local, cuenta por audio distinto
(no por repetición dentro de uno) y devuelve la cita con su marca de tiempo para poder
verificarla contra el audio, como exige EVIDENCIA.md.
"""
import pathlib, re, collections

CRUDO = pathlib.Path(__file__).parent / "crudo"

TEMAS = {
    "Planificación fuera de jornada": r"planific|prepar(o|ar|ando) (la |las |mi )?(sesi|clase|material)|de la noche|en la tarde|en casa|fin de semana",
    "Fichas diferenciadas y niveles": r"ficha[s]? diferenciad|diferenciad[oa]s? por nivel|ficha[s]? de (refuerzo|nivelaci)|por niveles|rebajar",
    "Necesidades específicas y DUA": r"\bDUA\b|autis|sordomud|discapacidad|retraso|hiperactiv|déficit de aten|SANE",
    "Apoyo e interés de los padres": r"no apoyan|desinter|no revisan|no asisten|no responden|no vienen|poco compromiso|no les interesa|apoderad",
    "Uso de IA y herramientas": r"\bIA\b|inteligencia artificial|chat ?gpt|chaje tipi|gemini|copilot|claud(e|ia)|perplexity",
    "Registro, papel y SIAGIE": r"registro auxiliar|siagie|ciagie|cigi|en papel|sello|informe[s]?|conclusiones descriptivas",
    "Infraestructura y conectividad": r"internet|cañón|canon|proyector|malogrado|desconectad|computadora[s]?|usb|pantalla",
    "Evaluación y criterios": r"criterio[s]? de evaluaci|instrumento|lista de cotejo|nivel de logro|evaluaci[óo]n formativa|rúbrica",
    "Concursos y nivel académico": r"concurso|conamat|conamá|pisa|olimpiada|nivel académico|no saben leer",
    "Interrupciones institucionales": r"ugel|minedu|ministerio|imprevist|charla|actividad(es)? extracurricular|monitoreo|supervisi",
    "Comunicación fuera de horario": r"11 de la noche|once de la noche|whatsapp|llaman|domingo|fuera de horario|hora de atenci",
}

SEG = re.compile(r"^\*\*\[(\d+:\d+)\]\*\*\s*(.+)$")


def main():
    docs = {}
    for ronda in sorted(d for d in CRUDO.iterdir() if d.is_dir()):
        for f in sorted((ronda / "texto").glob("*.md")):
            segs = []
            for linea in f.read_text(encoding="utf-8").splitlines():
                m = SEG.match(linea.strip())
                if m:
                    segs.append((m.group(1), m.group(2)))
            docs[f"{ronda.name}/{f.stem}"] = segs

    print(f"Corpus: {len(docs)} audios, {sum(len(v) for v in docs.values())} segmentos\n")
    for tema, patron in TEMAS.items():
        rx = re.compile(patron, re.I)
        hits = collections.defaultdict(list)
        for doc, segs in docs.items():
            for ts, txt in segs:
                if rx.search(txt):
                    hits[doc].append((ts, txt))
        total = sum(len(v) for v in hits.values())
        print("=" * 78)
        print(f"{tema}")
        print(f"  {len(hits)} de {len(docs)} audios · {total} menciones")
        # los 3 audios con más menciones, 2 citas cada uno
        for doc, cs in sorted(hits.items(), key=lambda kv: -len(kv[1]))[:3]:
            print(f"  ── {doc}  ({len(cs)})")
            for ts, txt in cs[:2]:
                print(f"       [{ts}] {txt[:150]}")
        print()


if __name__ == "__main__":
    main()
