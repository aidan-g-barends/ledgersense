// NEW: makes the browser download text as a file, without any server round trip.
export function downloadText(fileName: string, text: string, type = "text/csv;charset=utf-8") {
  // WHY the BOM (\uFEFF): tells Excel the file is UTF-8, so "é" doesn't show up as "Ã©"
  const blob = new Blob(["\uFEFF", text], { type });
  const url = URL.createObjectURL(blob); // a temporary in-memory URL for the blob
  const link = document.createElement("a");
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  link.remove();
  // WHY setTimeout: give the browser a moment to start the download before freeing the memory
  setTimeout(() => URL.revokeObjectURL(url), 0);
}