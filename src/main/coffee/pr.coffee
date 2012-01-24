$ = jQuery
$ ->
  # it goes in here
  $("#trust-toggle").click (e) ->
    e.preventDefault()
    console.log "clicked"
    $("#trust").slideToggle('fast')
    false
  return