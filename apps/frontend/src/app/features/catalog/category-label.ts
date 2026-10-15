/**
 * Etiquetas de categoría para la vista. La API usa el nombre del enum en inglés
 * (`TECHNOLOGY`/`HOME`/`CLOTHING`) y el español vive solo aquí, en presentación.
 */
const CATEGORY_LABELS: Readonly<Record<string, string>> = {
  TECHNOLOGY: 'Tecnología',
  HOME: 'Hogar',
  CLOTHING: 'Ropa',
};

/** Etiqueta en español de una categoría; si llega una desconocida, se muestra tal cual. */
export function categoryLabel(category: string): string {
  return CATEGORY_LABELS[category] ?? category;
}
