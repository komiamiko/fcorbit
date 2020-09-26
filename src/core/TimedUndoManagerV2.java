package core;

import java.util.ArrayDeque;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Independently written replacement for {@link javax.swing.undo.UndoManager},
 * serving as a class which can manage a history of edits which can be undone
 * and redone as requested.
 * <br/>
 * Notably, this version also collapses edits made within a short time frame
 * into one edit. Without this feature, every character typed would be its own
 * edit, and trying to undo/redo through that history would be unwieldy.
 * After {@link TimedUndoManagerV2#inactivityMs} ms of inactivity, an edit is registered.
 * <br/>
 * Another major reason to avoid {@link javax.swing.undo.UndoManager} is its flawed notion
 * of "significant edits." Suppose we have this sequence of edits:
 * {@code [A, b, c, D, e, f, G]} where uppercase letters denote a significant edit.
 * That undo manager will always undo up to and including the first significant edit.
 * The first undo will rewind to {@code [A, b, c, D, e, f]}, then the next undo will rewind to
 * {@code [A, b, c]}. The first redo after that will fastforward to
 * {@code [A, b, c, D]}, then the next redo will fastforward to {@code [A, b, c, D, e, f, G]}.
 * Notice how {@code [A, b, c]} and {@code [A, b, c, D]} are different, yet they are
 * supposedly at the same point in history (1 undo from the farthest), reached simply
 * by a different sequence of undo and redo. This quite horribly violates our intuition
 * about how undo and redo is supposed to work, and is confusing to users.
 * If you undo and then redo, you should always end up back where you started.
 * {@link TimedUndoManagerV2} doesn't have this pitfall. If a single undo actually
 * rewinds multiple edits, this is invisible to the user, and as far as they're concerned,
 * it might as well be one edit.
 * 
 * @author komiamiko
 * @version 1.0
 */
public class TimedUndoManagerV2 implements UndoableEditListener {
	
	/**
	 * Performance factor, relevant in {@link #checkTrim()}.
	 * Once this many low-level entries are removed in one call,
	 * it finishes the remainder of that high-level entry,
	 * and then stops.
	 * <br/>
	 * Without a stop like this, when {@link #pastLimit} is reduced
	 * in the settings and the history is full, the application
	 * will try to trim away all the excess in one go,
	 * which may be a lot. It may even cause the application to freeze.
	 * With this limiter, the application will at most lag somewhat,
	 * but still be usable, since in each burst it only does a
	 * small portion of the work.
	 * <br/>
	 * Gradually freeing up memory like this is the point, but it
	 * can also be a curse - if the user wants to free up memory right away,
	 * it won't happen.
	 */
	public static final int TRIM_STOP = 50;
	/**
	 * Some preliminary research shows the average length of a FCML line
	 * is about 62 characters.
	 */
	public static final int AVERAGE_LINE_LENGTH = 62;
	
	/**
	 * In milliseconds, how long to wait to register an edit.
	 * This allows many tiny edits (individual characters typed) to be combined
	 * into the single edit the user would expect.
	 */
	public long inactivityMs;
	/**
	 * Low-level limit for number of past edits to retain.
	 * These edits are numerous - generally one per character typed.
	 * <br/>
	 * The average length of a line is {@link #AVERAGE_LINE_LENGTH},
	 * and users want the high-level limit to be 500.
	 * Hence, the default limit.
	 */
	public int pastLimit;
	/**
	 * Time of the last edit.
	 */
	public long lastEditTime;
	/**
	 * Value to add to all times we record.
	 * This is used to artificially create gaps without needing to wait.
	 */
	public long timeOffset;
	/**
	 * The past, represented as a deque, where the left (first) is the oldest edit
	 * and the right (last) is the most recent edit.
	 * An undo operation moves edits from the right (last) of the {@link #past}
	 * into the left (first) of the {@link #future}.
	 */
	public ArrayDeque<TimedEdit> past;
	/**
	 * The future, represented as a deque, where the left (first) is the oldest edit
	 * and the right (last) is the most recent edit.
	 * A redo operation moves edits from the left (first) of the {@link #future}
	 * into the right (last) of the {@link #past}.
	 */
	public ArrayDeque<TimedEdit> future;
	
	/**
	 * Default constructor.
	 */
	public TimedUndoManagerV2() {
		inactivityMs = 1000;
		pastLimit = AVERAGE_LINE_LENGTH * 500;
		lastEditTime = System.currentTimeMillis();
		timeOffset = -lastEditTime;
		past = new ArrayDeque<>();
		future = new ArrayDeque<>();
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
		// this future is no longer possible
		// since we did something different
		future.clear();
		// record new event in the past
		TimedEdit edit = new TimedEdit(
				e.getEdit(),
				lastEditTime = System.currentTimeMillis() + timeOffset
				);
		past.addLast(edit);
		// trim past if needed
		checkTrim();
	}
	
	/**
	 * Inject a time gap between the real-time past and the real-time future,
	 * with size equal to the inactivity threshold.
	 * If you do this before and after making an edit, that edit is guaranteed
	 * to be considered alone by the undo manager.
	 * Similarly, this can be used to break up a chunk of edits that would normally
	 * be treated as one big edit.
	 */
	public void induceGap() {
		timeOffset += inactivityMs;
	}
	
	/**
	 * Check if the past is too long. If so, trim it by removing
	 * the oldest entries. Return how many entries were removed.
	 * 
	 * @return number of entries removed. 0 means nothing happened.
	 * @see #TRIM_STOP
	 */
	public int checkTrim() {
		int nremoved = 0;
		// start a new deletion spree if
		// the past is too long and
		while(past.size() > pastLimit &&
				// we have not reached the deletion limit yet
				nremoved < TRIM_STOP
				) {
			// let's delete some things!
			// get the oldest edit
			TimedEdit current = past.pollFirst();
			// keep going forward as long as there are edits and
			while(!past.isEmpty() &&
					// the time gap is small enough
					past.peekFirst().time - current.time < inactivityMs) {
				// delete this one too
				current.die();
				nremoved++;
				// grab the next one
				current = past.pollFirst();
			}
			// handle the leftover one
			current.die();
			nremoved++;
		}
		return nremoved;
	}
	
	/**
	 * Try to undo. Return true on success, false if it failed.
	 * If it returns false, it is guaranteed to do nothing (no state is modified).
	 * 
	 * @return true if the undo happened, false if nothing happened
	 */
	public boolean tryUndo() {
		// just because we can, means we should
		boolean should = canUndo();
		if(should) {
			undo();
		}
		return should;
	}
	
	/**
	 * Can we undo once?
	 * 
	 * @return true if we can undo, false if no undo is possible
	 */
	public boolean canUndo() {
		// internally we assume all edits are undoable
		// so we don't bother asking the edit
		return !past.isEmpty();
	}
	
	/**
	 * Undo once, assuming undo is possible.
	 * Use {@link #canUndo()} first to check if undo is possible.
	 * 
	 * @throws CannotUndoException if one of the edits throws
     *   {@link CannotUndoException} (meaning it failed to undo)
	 */
	public void undo() throws CannotUndoException {
		// get the most immediate edit
		TimedEdit current = past.pollLast();
		// keep going back as long as there are edits and
		while(!past.isEmpty() &&
				// the time gap is small enough
				current.time - past.peekLast().time < inactivityMs) {
			// undo this edit, it is now part of the future
			current.undo();
			future.addFirst(current);
			// grab the next one
			current = past.pollLast();
		}
		// handle the leftover one
		current.undo();
		future.addFirst(current);
	}
	
	/**
	 * Try to redo. Return true on success, false if it failed.
	 * If it returns false, it is guaranteed to do nothing (no state is modified).
	 * 
	 * @return true if the redo happened, false if nothing happened
	 */
	public boolean tryRedo() {
		// just because we can, means we should
		boolean should = canRedo();
		if(should) {
			redo();
		}
		return should;
	}
	
	/**
	 * Can we redo once?
	 * 
	 * @return true if we can redo, false if no redo is possible
	 */
	public boolean canRedo() {
		// internally we assume all edits are undoable
		// so we don't bother asking the edit
		return !future.isEmpty();
	}
	
	/**
	 * Redo once, assuming redo is possible.
	 * Use {@link #canRedo()} first to check if redo is possible.
	 * 
	 * @throws CannotRedoException if one of the edits throws
     *   {@link CannotRedoException} (meaning it failed to redo)
	 */
	public void redo() throws CannotRedoException {
		// get the most immediate edit
		TimedEdit current = future.pollFirst();
		// keep going forward as long as there are edits and
		while(!future.isEmpty() &&
				// the time gap is small enough
				future.peekFirst().time - current.time < inactivityMs) {
			// redo this edit, it is now part of the past
			current.redo();
			past.addLast(current);
			// grab the next one
			current = future.pollFirst();
		}
		// handle the leftover one
		current.redo();
		past.addLast(current);
	}
	
	/**
	 * Wrapper around an arbitrary edit which tracks time information.
	 * Intended for use in the context of {@link TimedUndoManagerV2}.
	 * All interface methods defer to the underlying edit.
	 * Some which are normally important are ignored by the undo manager anyway.
	 * 
	 * @author komiamiko
	 */
	public static class TimedEdit implements UndoableEdit {
		
		/**
		 * The edit being wrapped.
		 */
		public UndoableEdit edit;
		/**
		 * Timestamp of when this edit was created.
		 */
		public long time;
		
		/**
		 * Direct constructor with a given edit and time.
		 * 
		 * @param edit arbitrary edit to wrap
		 * @param time time the edit was created
		 */
		public TimedEdit(UndoableEdit edit, long time) {
			this.edit = edit;
			this.time = time;
		}

		@Override
		public void undo() throws CannotUndoException {
			edit.undo();
		}

		@Override
		public boolean canUndo() {
			return edit.canUndo();
		}

		@Override
		public void redo() throws CannotRedoException {
			edit.redo();
		}

		@Override
		public boolean canRedo() {
			return edit.canRedo();
		}

		@Override
		public void die() {
			edit.die();
		}

		@Override
		public boolean addEdit(UndoableEdit anEdit) {
			return edit.addEdit(anEdit);
		}

		@Override
		public boolean replaceEdit(UndoableEdit anEdit) {
			return edit.replaceEdit(anEdit);
		}

		@Override
		public boolean isSignificant() {
			return edit.isSignificant();
		}

		@Override
		public String getPresentationName() {
			return edit.getPresentationName();
		}

		@Override
		public String getUndoPresentationName() {
			return edit.getUndoPresentationName();
		}

		@Override
		public String getRedoPresentationName() {
			return edit.getRedoPresentationName();
		}
		
	}

}
