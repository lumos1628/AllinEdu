-- Desempeños de 4.º grado, columna «logra el nivel esperado del ciclo IV».
-- Alcance declarado del proyecto: 4.º grado, áreas de Matemática y Comunicación.
--
-- Extraídos de la edición publicada del Programa Curricular (396 pp.) por
-- coordenadas de palabra (pdftotext -bbox), porque el documento presenta los
-- desempeños en dos columnas que las extracciones de texto plano entrelazan.
-- Cada texto se leyó antes de cargarlo.
--
-- NO se carga «Se comunica oralmente en su lengua materna»: su extracción salió
-- con el texto de las dos columnas entrelazado («seleccionando datos orales que
-- escucha, seleccionando datos…») y se descartó en lugar de corregirla a mano.
-- Tampoco las otras 24 competencias del grado: quedan declaradas como pendientes.
--
-- De «Resuelve problemas de cantidad» se omitió una viñeta que empieza a media
-- frase en el salto de página («Estrategias de cálculo mental o escrito…»).

delete from public.desempenos_grado
 where grado_id = (select g.id from public.grados_educativos g
                   where g.numero = 4
                     and g.nivel_id = (select id from public.niveles_educativos where codigo='PRIMARIA'));

with g as (select id from public.grados_educativos
            where numero = 4
              and nivel_id = (select id from public.niveles_educativos where codigo='PRIMARIA'))
insert into public.desempenos_grado
    (competencia_id, grado_id, descripcion, fuente_url, fuente_pagina, exige_material_concreto)
select c.id, g.id, m.descripcion,
       'https://www.minedu.gob.pe/curriculo/pdf/programa-curricular-educacion-primaria.pdf',
       m.pagina,
       m.descripcion ilike '%material concreto%'
from g, (values
    ('COM_LEE','Identifica información explícita y relevante que se encuentra en distintas partes del texto. Distingue esta información de otra semejante, en la que selecciona datos específicos, en diversos tipos de textos de estructura simple, con algunos elementos complejos , así como vocabulario variado, de acuerdo a las temáticas abordadas.','165'),
    ('COM_LEE','Deduce características implícitas de personajes, animales, objetos y lugares, y determina el significado de palabras y frases según el contexto, así como de expresiones con sentido figurado (refranes, comparaciones, etc.). Establece relaciones lógicas de intención-finalidad y tema y subtema, a partir de información relevante explícita e implícita.','165'),
    ('COM_LEE','Predice de qué tratará el texto, a partir de algunos indicios como subtítulos, colores y dimensiones de las imágenes, índice, tipografía, negritas, subrayado, etc.; asimismo, contrasta la información del texto que lee.','165'),
    ('COM_LEE','Explica el tema, el propósito, las motivaciones de personas y personajes, las comparaciones y personificaciones, así como las enseñanzas y los valores del texto, clasificando y sintetizando la información.','165'),
    ('COM_LEE','Opina acerca del contenido del texto, explica el sentido de algunos recursos textuales (uso de negritas, mayúsculas, índice, tipografía, subrayado, etc.), a partir de su experiencia y contexto, y justifica sus preferencias cuando elige o recomienda textos según sus necesidades, intereses y su relación con otros textos, con el fin de reflexionar sobre los textos que lee.','165'),
    ('COM_ESCRIBE','Adecúa el texto a la situación comunicativa considerando el propósito comunicativo, el destinatario y las características más comunes del tipo textual. Distingue el registro formal del informal; para ello, recurre a su experiencia y a algunas fuentes de información complementaria.','175'),
    ('COM_ESCRIBE','Escribe textos de forma coherente y cohesionada. Ordena las ideas en torno a un tema y las desarrolla para ampliar la información, sin contradicciones, reiteraciones innecesarias o digresiones. Establece relaciones entre las ideas, como adición, causa-efecto y consecuencia, a través de algunos referentes y conectores. Incorpora un vocabulario que incluye sinónimos y algunos términos propios de los campos del saber.','175'),
    ('COM_ESCRIBE','Utiliza recursos gramaticales y ortográficos (por ejemplo, el punto seguido y las comas enumerativas) que contribuyen a dar sentido a su texto, e incorpora algunos recursos textuales (por ejemplo, el tamaño de la letra) para reforzar dicho sentido. Emplea comparaciones y adjetivaciones para caracterizar personas, personajes y escenarios, y elabora rimas y juegos verbales apelando al ritmo y la musicalidad de las palabras, con el fin de expresar sus experiencias y emociones.','175'),
    ('COM_ESCRIBE','Revisa el texto para determinar si se ajusta a la situación comunicativa, si existen contradicciones o reiteraciones innecesarias que afectan la coherencia entre las ideas, o si el uso de conectores y referentes asegura la cohesión entre ellas. También, revisa el uso de los recursos ortográficos que empleó en su texto y verifica si falta alguno (como el punto aparte), con el fin de mejorarlo.','175'),
    ('COM_ESCRIBE','Explica el efecto de su texto en los lectores considerando su propósito al momento de escribirlo. Asimismo, explica la importancia de los aspectos gramaticales y ortográficos más comunes.','175'),
    ('MAT_CANTIDAD','Establece relaciones entre datos y una o más acciones de agregar, quitar, comparar, igualar, reiterar, agrupar, repartir cantidades y combinar colecciones, para transformarlas en expresiones numéricas (modelo) de adición, sustracción, multiplicación y división con números naturales de hasta cuatro cifras.','238'),
    ('MAT_CANTIDAD','Establece relaciones entre datos y acciones de partir una unidad o una colección de objetos en partes iguales y las transforma en expresiones numéricas (modelo) de fracciones usuales, adición y sustracción de estas.','238'),
    ('MAT_CANTIDAD','Mide, estima y compara la masa (kilogramo, gramo) y el tiempo (año, hora, media hora y cuarto de hora) seleccionando unidades convencionales.','238'),
    ('MAT_CANTIDAD','Realiza afirmaciones sobre la conformación de la unidad de millar y las explica con material concreto.','238'),
    ('MAT_CANTIDAD','Realiza afirmaciones sobre las equivalencias entre fracciones y las explica con ejemplos concretos. Asimismo, explica la comparación entre fracciones, así como su proceso de resolución y los resultados obtenidos.','238'),
    ('MAT_REGULARIDAD','Establece relaciones entre datos de hasta dos equivalencias y las trasforma en igualdades que contienen adiciones o sustracciones, o multiplicaciones o divisiones.','249'),
    ('MAT_REGULARIDAD','Establece relaciones entre los datos de una regularidad y los transforma en patrones de repetición (que combinan criterios perceptuales y un criterio geométrico de simetría) o patrones aditivos o multiplicativos (con números de hasta 4 cifras).','249'),
    ('MAT_REGULARIDAD','Expresa, usando lenguaje algebraico (ícono y operaciones) y diversas representaciones, su comprensión de la regla de formación de un patrón, de la igualdad (con un término desconocido) y del signo igual, distinguiéndolo de su uso en el resultado de una operación.','249'),
    ('MAT_REGULARIDAD','Describe la relación de cambio de una magnitud con respecto de otra, apoyándose en tablas o dibujos.','249'),
    ('MAT_REGULARIDAD','Emplea estrategias heurísticas o estrategias de cálculo (duplicar o repartir en cada lado de la igualdad, relación inversa entre operaciones), para encontrar equivalencias, completar, crear o continuar patrones, o para encontrar relaciones de cambio entre dos magnitudes.','249'),
    ('MAT_REGULARIDAD','Hace afirmaciones sobre la equivalencia entre expresiones; para ello, usa nocionalmente las propiedades de la igualdad: uniformidad y cancelativa.','249'),
    ('MAT_REGULARIDAD','Hace afirmaciones sobre las regularidades, las relaciones de cambio entre magnitudes, así como los números o elementos que siguen en un patrón, y las justifica con sus experiencias concretas. Así también, justifica sus procesos de resolución.','249'),
    ('MAT_FORMA','Establece relaciones entre las características de objetos reales o imaginarios, los asocia y representa con formas bidimensionales (polígonos) y sus elementos, así como con su perímetro, medidas de longitud y superficie; y con formas tridimensionales (cubos y prismas de base cuadrangular), sus elementos y su capacidad.','258'),
    ('MAT_FORMA','Establece relaciones entre los datos de ubicación y recorrido de los objetos, personas y lugares cercanos, así como la traslación de los objetos o figuras, y los expresa en gráficos o croquis teniendo a los objetos y lugares fijos como puntos de referencia.','258'),
    ('MAT_FORMA','Expresa con dibujos su comprensión sobre los elementos de cubos y prismas de base cuadrangular: caras, vértices, aristas; también, su comprensión sobre los elementos de los polígonos: ángulos rectos, número de lados y vértices; así como su comprensión sobre líneas perpendiculares y paralelas usando lenguaje geométrico.','258'),
    ('MAT_FORMA','Expresa con material concreto o gráficos su comprensión sobre el perímetro y la medida de capacidad de los recipientes para determinar cuántas veces se puede llenar uno con el otro. Asimismo, su comprensión sobre la medida de la superficie de objetos planos, de manera cualitativa y con representaciones concretas estableciendo “es más extenso que”, “es menos extenso que” (superficie asociada a la noción de extensión) y su conservación.','258'),
    ('MAT_FORMA','Expresa con gráficos o croquis los desplazamientos y posiciones de objetos, personas y lugares cercanos, así como sus traslaciones con relación a objetos fijos como puntos de referencia. Ejemplo: El estudiante podría dar instrucciones a partir de objetos del entorno para ubicar otros, o a partir de lugares del entorno para ubicarse o ubicar a otros.','258'),
    ('MAT_FORMA','Emplea estrategias, recursos y procedimientos como la composición y descomposición, la visualización, así como el uso de las cuadrículas, para construir formas simétricas, ubicar objetos y trasladar figuras, usando recursos. Así también, usa diversas estrategias para medir, de manera exacta o aproximada (estimar), la medida de los ángulos respecto al ángulo recto, la longitud, el perímetro (metro y centímetro), la superficie (unidades patrón) y la capacidad (en litro y con fracciones) de los objetos, y hace conversiones de unidades de longitud. Emplea la unidad de medida, convencional o no convencional, según convenga, así como algunos instrumentos de medición (cinta métrica, regla, envases o recipientes).','258'),
    ('MAT_FORMA','Hace afirmaciones sobre algunas relaciones entre elementos de las formas y su desarrollo en el plano, y explica sus semejanzas y diferencias mediante ejemplos concretos o dibujos con base en su exploración o visualización. Así también, explica el proceso seguido. Ejemplo: El estudiante podría decir: “Un cubo se puede construir con una plantilla que contenga 6 cuadrados del mismo tamaño”.','258'),
    ('MAT_DATOS','Representa las características y el comportamiento de datos cualitativos (por ejemplo, color de ojos: pardos, negros; profesión: médico, abogado, etc.) y cuantitativos discretos (por ejemplo: número de hermanos: 3, 2; cantidad de goles: 2, 4, 5, etc.) de una población, a través de pictogramas verticales y horizontales (cada símbolo representa más de una unidad), gráficos de barras con escala dada (múltiplos de 10) y la moda como la mayor frecuencia, en situaciones de interés o un tema de estudio.','268'),
    ('MAT_DATOS','Expresa su comprensión de la moda como la mayor frecuencia y la media aritmética como punto de equilibrio; así como todos los posibles resultados de la ocurrencia de sucesos cotidianos usando las nociones “seguro”, “más probable” y “menos probable”.','268'),
    ('MAT_DATOS','Lee gráficos de barras con escala, tablas de doble entrada y pictogramas de frecuencias con equivalencias, para interpretar la información a partir de los datos contenidos en diferentes formas de representación y de la situación estudiada.','268'),
    ('MAT_DATOS','Recopila datos mediante encuestas sencillas o entrevistas cortas con preguntas adecuadas empleando procedimientos y recursos; los procesa y organiza en listas de datos, tablas de doble entrada o tablas de frecuencia, para describirlos y analizarlos.','268'),
    ('MAT_DATOS','Selecciona y emplea procedimientos y recursos como el recuento, el diagrama, las tablas de frecuencia u otros, para determinar la media aritmética como punto de equilibrio, la moda como la mayor frecuencia y todos los posibles resultados de la ocurrencia de sucesos cotidianos.','268'),
    ('MAT_DATOS','Predice que la posibilidad de ocurrencia de un suceso es mayor que otro. Así también, explica sus decisiones y conclusiones a partir de la información obtenida con base en el análisis de datos.','268')
) as m(cod, descripcion, pagina)
join public.competencias c on c.codigo = m.cod
     and c.version_curricular_id = (select id from public.versiones_curriculares where codigo='CNEB_2016')
on conflict (competencia_id, grado_id, descripcion) do update
    set fuente_pagina = excluded.fuente_pagina,
        exige_material_concreto = excluded.exige_material_concreto;
