import { useEffect, useLayoutEffect, useRef, useState } from 'react';
import { Box, Link, Typography } from '@mui/material';

/**
 * Long free-text cell content — work plans, work reports, lecture summaries,
 * leave reasons — shown readably inside a table row.
 *
 * These fields were previously rendered with `noWrap`, which cut them to a
 * single line and put the rest in a `title` tooltip. That hid most of the text
 * on desktop and *all* of it on a phone, where there is no hover to reveal a
 * tooltip. Here the text wraps over a few lines and the reader can open the
 * rest in place, which works the same with a mouse or a thumb.
 *
 * Newlines are preserved, so the bullet-per-line style people actually write
 * these reports in survives instead of collapsing into one run-on paragraph.
 *
 * @param {string}  text    the text to display
 * @param {number}  lines   lines shown before clamping (default 3)
 * @param {string}  variant MUI Typography variant (default 'body2')
 */
const ExpandableText = ({ text, lines = 3, variant = 'body2' }) => {
  const [expanded, setExpanded] = useState(false);
  // Whether the text is actually longer than the clamp. Drives whether the
  // toggle appears at all, so short entries stay visually clean.
  const [clamped, setClamped] = useState(false);
  const textRef = useRef(null);

  // Paging a table reuses these nodes for different rows; without this an
  // expanded row would leave the next page's row expanded too.
  useEffect(() => {
    setExpanded(false);
  }, [text]);

  useLayoutEffect(() => {
    const el = textRef.current;
    // Only measurable while clamped — once expanded, scrollHeight and
    // clientHeight match and the answer would always be "fits".
    if (!el || expanded) {
      return undefined;
    }

    const measure = () => setClamped(el.scrollHeight > el.clientHeight + 1);
    measure();

    // Column widths shift as the table reflows; re-measure so the toggle
    // appears or disappears to match.
    const observer = new ResizeObserver(measure);
    observer.observe(el);
    return () => observer.disconnect();
  }, [text, lines, expanded]);

  if (!text) {
    return (
      <Typography variant={variant} color="text.secondary">
        —
      </Typography>
    );
  }

  return (
    <Box>
      <Typography
        ref={textRef}
        variant={variant}
        sx={{
          whiteSpace: 'pre-wrap',
          // Break inside a word only as a last resort, so a pasted URL cannot
          // stretch the column past its maxWidth.
          overflowWrap: 'anywhere',
          ...(expanded
            ? {}
            : {
                display: '-webkit-box',
                WebkitBoxOrient: 'vertical',
                WebkitLineClamp: lines,
                overflow: 'hidden',
              }),
        }}
      >
        {text}
      </Typography>

      {(clamped || expanded) && (
        <Link
          component="button"
          // Without this the button defaults to type="submit" and would submit
          // any form the table happens to sit inside.
          type="button"
          variant="caption"
          underline="hover"
          onClick={() => setExpanded((open) => !open)}
          sx={{ mt: 0.25 }}
        >
          {expanded ? 'Show less' : 'Show more'}
        </Link>
      )}
    </Box>
  );
};

export default ExpandableText;
