/**
 * Builds a CSV string and triggers a browser download. Values containing
 * commas or quotes are quoted per RFC 4180.
 */
export const downloadCsv = (filename, headers, rows) => {
  const escape = (value) => {
    const text = String(value ?? '');
    return /[",\n]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
  };

  const csv = [headers, ...rows]
    .map((row) => row.map(escape).join(','))
    .join('\n');

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
};
