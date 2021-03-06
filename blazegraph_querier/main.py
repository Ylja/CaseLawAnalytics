from . import query_to_json, links_to_json
import traceback

import io
from flask import Flask, request, render_template, make_response, send_file
app = Flask(__name__)


@app.route('/')
def index():
    return render_template('index.html')

@app.route('/query_keyword', methods=['POST'])
def query():
    try:
        network_json = None
        network_csv = None
        if('keyword' in request.form):
            searchstring = request.form['keyword']
            only_linked = request.form.get('only_linked', False)
            nodes, links = query_to_json.query(searchstring,
                                                         only_linked=only_linked)
            network_json = query_to_json.to_sigma_json(nodes, links, searchstring)
            network_csv = query_to_json.to_csv(nodes)

        return render_template("index.html",
                               network_json=network_json,
                               network_csv=network_csv)
    except Exception as error:
        print(error)
        traceback.print_exc()
        return render_template("index.html",
                        error="Sorry, something went wrong!")


@app.route('/query_links', methods=['POST'])
def query_links():
    try:
        network_json = None
        network_csv = None
        if('links' in request.form):
            links_csv = request.form['links']
            title = request.form.get('title', 'Network')
            links_df, eclis = links_to_json.read_csv(io.StringIO(links_csv),
                                                     sep=',', header=None)
            graph = links_to_json.make_graph(links_df, eclis)

            if len(graph)==0:
                return render_template("index.html",
                                error="No resulting matches!")
            nodes, links = links_to_json.graph_to_network(graph)
            network_json = query_to_json.to_sigma_json(nodes, links, title)
            network_csv = query_to_json.to_csv(nodes)
        return render_template("index.html",
                               network_json=network_json,
                               network_csv=network_csv)
    except Exception as error:
        print(error)
        traceback.print_exc()
        return render_template("index.html",
                        error="Sorry, something went wrong!")

@app.route('/download-json/', methods=['POST'])
def download_json():
    network_json = ""
    if('network_json' in request.form):
        network_json = request.form['network_json']
    response = make_response(network_json)
    response.headers["Content-Disposition"] = "attachment; filename=network_json.json"
    return response

@app.route('/download-csv/', methods=['POST'])
def download_csv():
    network_csv = ""
    if('network_csv' in request.form):
        network_csv = request.form['network_csv']
    response = make_response(network_csv)
    response.headers["Content-Disposition"] = "attachment; filename=network_csv.csv"
    return response
