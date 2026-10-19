#!/usr/bin/env bash
# Descarga la documentación oficial vigente en la que se apoya el proyecto.
# Los PDF no se versionan (ver .gitignore); lo que se versiona es FUENTES.md
# con la URL, la norma, el número de páginas y el sha256 de cada archivo.
set -uo pipefail
DEST="$(cd "$(dirname "$0")" && pwd)/fuentes"
mkdir -p "$DEST"

descargar() {
    local nombre="$1" url="$2"
    local salida="$DEST/$nombre"
    if [ -s "$salida" ]; then
        printf '  ya estaba   %s\n' "$nombre"; return 0
    fi
    if curl -fsSL --max-time 300 -o "$salida" "$url" 2>/dev/null \
       && [ -s "$salida" ] && head -c4 "$salida" | grep -q '%PDF'; then
        printf '  descargado  %s\n' "$nombre"
    else
        rm -f "$salida"
        printf '  FALLO       %s\n' "$nombre"
        printf '%s\t%s\n' "$nombre" "$url" >> "$DEST/_fallidos.txt"
    fi
}

: > "$DEST/_fallidos.txt"
echo "CURRÍCULO"
descargar "CNEB_RM281-2016.pdf" \
  "https://www.minedu.gob.pe/curriculo/pdf/curriculo-nacional-de-la-educacion-basica.pdf"
descargar "PROGRAMA_CURRICULAR_PRIMARIA.pdf" \
  "https://www.minedu.gob.pe/curriculo/pdf/programa-curricular-educacion-primaria.pdf"

echo "EVALUACIÓN"
descargar "RVM094-2020_evaluacion.pdf" \
  "https://cdn.www.gob.pe/uploads/document/file/662983/RVM_N__094-2020-MINEDU.pdf"
descargar "RVM048-2024_modifica_evaluacion.pdf" \
  "https://cepmm.edu.pe/images/doc2024/RVM0048-2024-EDModificaNormaEvaluacion.pdf"

echo "AÑO ESCOLAR Y GESTIÓN"
descargar "RM501-2025_norma_tecnica_2026.pdf" \
  "https://repositorio.minedu.gob.pe/bitstream/handle/20.500.12799/11753/Norma%20T%C3%A9cnica%20para%20el%20A%C3%B1o%20Escolar%202026%20en%20las%20Instituciones%20y%20Programas%20Educativos%20P%C3%BAblicos%20y%20Privados%20de%20la%20Educaci%C3%B3n%20B%C3%A1sica.pdf?sequence=1&isAllowed=y"
descargar "CARTILLA_semanas_de_gestion.pdf" \
  "https://repositorio.minedu.gob.pe/bitstream/handle/20.500.12799/11636/Cartilla%20sobre%20el%20desarrollo%20de%20las%20semanas%20de%20gesti%C3%B3n%20en%20la%20Educaci%C3%B3n%20B%C3%A1sica.pdf?sequence=1&isAllowed=y"

echo "JORNADA LABORAL"
descargar "LEY29944_reforma_magisterial.pdf" \
  "https://www2.congreso.gob.pe/sicr/cendocbib/con4_uibd.nsf/2AB94C1F180F327605257AF6007B9176/\$FILE/Ley_29944.pdf"
descargar "DS004-2013-ED_reglamento.pdf" \
  "https://www2.congreso.gob.pe/sicr/cendocbib/con6_uibd.nsf/7358D0E4DFCA05E705258ABC00513DB4/\$FILE/decreto_supre_004_reglamento_ley_29944_compressed.pdf"

echo "ORIENTACIÓN (no son norma exigible)"
descargar "CARTILLA_planificacion_primaria_2017.pdf" \
  "https://www.minedu.gob.pe/curriculo/pdf/cartilla-planificacion-curricular.pdf"
descargar "CARTILLA_sesion_multigrado.pdf" \
  "https://repositorio.minedu.gob.pe/bitstream/handle/20.500.12799/10342/Cartilla%20de%20sesi%C3%B3n%20de%20aprendizaje%20para%20aulas%20multigrado%20con%20curr%C3%ADculo%20nacional%20C%C3%B3mo%20planificar%20sesiones%20de%20aprendizaje%20para%20aulas%20multigrado.pdf?sequence=1&isAllowed=y"
descargar "RUBRICAS_observacion_de_aula.pdf" \
  "https://repositorio.minedu.gob.pe/bitstream/handle/20.500.12799/5332/R%C3%BAbricas%20de%20observaci%C3%B3n%20de%20aula%20para%20la%20evaluaci%C3%B3n%20del%20desempe%C3%B1o%20docente%20manual%20de%20aplicaci%C3%B3n.pdf?sequence=1"

echo
if [ -s "$DEST/_fallidos.txt" ]; then
    echo "Fallaron:"; cat "$DEST/_fallidos.txt"
else
    echo "Todo descargado."; rm -f "$DEST/_fallidos.txt"
fi
