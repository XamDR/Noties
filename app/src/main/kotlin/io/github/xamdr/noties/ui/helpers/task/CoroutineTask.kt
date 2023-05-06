package io.github.xamdr.noties.ui.helpers.task

import kotlinx.coroutines.*

abstract class CoroutineTask<Params, Progress, Result> {

	private var job: Job? = null

	open fun onPreExecute() {}

	abstract fun doInBackground(vararg  params: Params): Result

	open fun onPostExecute(result: Result) {}

	open fun onProgressUpdate(vararg progress: Progress) {}

	open fun onCancelled() {}

	fun execute(vararg  params: Params) {
		job = CoroutineScope(Dispatchers.Main).launch {
			onPreExecute()
			val result = withContext(Dispatchers.IO) { doInBackground(*params) }
			onPostExecute(result)
		}
	}

	fun cancel() {
		if (job != null && job?.isActive == true) {
			job?.cancel()
		}
		onCancelled()
	}

	fun publishProgress(vararg progress: Progress) {
		CoroutineScope(Dispatchers.Main).launch {
			onProgressUpdate(*progress)
		}
	}
}